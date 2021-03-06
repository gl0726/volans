package com.haizhi.volans.loader.scala.config.check

import java.lang.reflect.Type
import java.util

import com.google.gson.reflect.TypeToken
import com.haizhi.volans.common.flink.base.java.util.FileUtil
import com.haizhi.volans.common.flink.base.scala.exception.ErrorCode
import com.haizhi.volans.common.flink.base.scala.util.JSONUtils
import com.haizhi.volans.loader.scala.config.exception.VolansCheckException
import com.haizhi.volans.loader.scala.config.parameter.Parameter
import com.haizhi.volans.loader.scala.config.schema.{Keys, SchemaVo}
import com.haizhi.volans.loader.scala.config.streaming.error.{DirtyData, ErrorInfo, LogInfo}
import com.haizhi.volans.loader.scala.config.streaming.{FileConfig, StreamingConfig, error}
import com.haizhi.volans.loader.scala.config.streaming.flink.FlinkConfig
import com.haizhi.volans.loader.scala.config.streaming.sink.Sinks
import com.haizhi.volans.loader.scala.config.streaming.source.{KafkaSourceConfig, Source}
import com.haizhi.volans.loader.scala.util.HDFSUtils
import com.hzxt.volans.loader.java.StoreType
import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}

/**
 * @author gl 
 * @create 2020-11-02 13:52 
 */
object StreamingConfigHelper {
  private val LOG: Logger =  LoggerFactory.getLogger(classOf[StreamingConfigHelper])
  /**
   * 根据main函数args传参，创建全局streamingConfig
   *
   * @param inputParam args[]
   */
  def parse(inputParam: String): StreamingConfig = {
    if (StringUtils.isBlank(inputParam)) {
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} -input 参数为空，请输入正确路径")
    }
    val content: String = downloadParamIfNecessary(inputParam)
    if (StringUtils.isBlank(content))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} inputParam args[] 获取参数数据为空，无法获取全局参数")
    LOG.info("SparkArgs：" + content)
    doParse(content)
  }

  /**
   * 根据启动参数获取不同位置的全局配置
   *
   * @param inputParamPath args[0]
   * @return 返回json字符串
   */
  private def downloadParamIfNecessary(inputParamPath: String): String = {
    var content: String = null
    LOG.info(s"flink driver args: ${inputParamPath}")
    if (inputParamPath.startsWith("/") || inputParamPath.startsWith("file:///") || inputParamPath.startsWith("file:/")) {
      content = FileUtil.readFileToString(inputParamPath, "utf-8")
    } else if (inputParamPath.startsWith("{")) {
      content = inputParamPath
    } else if (inputParamPath.startsWith("hdfs://")) {
      content = HDFSUtils.readFileContent(inputParamPath)
    } else {
      content = FileUtil.readThisPath(inputParamPath)
    }
    content
  }


  def getSchema(map: util.Map[String, AnyRef]): SchemaVo = {
    try{
      val schemaVo: SchemaVo = JSONUtils.fromJson(JSONUtils.toJson(map.get(Parameter.SCHEMA)), new TypeToken[SchemaVo]() {}.getType)
      schemaVo.check
      schemaVo
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} ${ErrorCode.getJSON(e.getMessage)} <- getSchema函数解析参数异常")
    }
  }

  /**
   * 执行函数，通过json参数创建全局配置类：streamingConfig
   *
   * @param param json参数
   */
  def doParse(param: String): StreamingConfig = {
    val map: util.Map[String, AnyRef] = JSONUtils.jsonToMap(param)
    //检查关键参数
    CheckHelper.checkMap(map)
    //获取error
    val logInfo: LogInfo = getLogInfo(map)
    Keys.logInfo = logInfo
    LOG.info(s" info ${logInfo.storeType} error : $logInfo")
    //先获取dirty，因为后面任何异常都需要taskInstanceId参数才可展示异常
    val dirtyData: DirtyData = getDirtyData(map)
    Keys.taskInstanceId = dirtyData.taskInstanceId
    LOG.info(s" info ${dirtyData.storeType} dirty : $dirtyData")
    //获取source
    val source: Source = getSource(map)
    LOG.info(s" info ${source.storeType} source, config : ${source.kafkaSourceConfig}")
    //获取sinksJson
    val sinksJson: String = getSinks(map)
    LOG.info(s" info sinks Json : $sinksJson")
    //获取schema
    val schemaVo: SchemaVo = getSchema(map)
    LOG.info(s" info schemaVo : $schemaVo")
    //获取flinkConfig
    val flinkConfig: FlinkConfig = getFlinkConfig(map)
    LOG.info(s" info flinkConfig : $flinkConfig")
    LOG.info(s" info checkpoint : ${flinkConfig.checkPoint}")

    StreamingConfig(source, sinksJson, schemaVo, ErrorInfo(dirtyData, logInfo), flinkConfig)
  }

  /**
   * 生成对应的kafkaSource
   *
   * @param map 全局参数map
   * @return source类
   */
  def getSource(map: util.Map[String, AnyRef]): Source = {
    try{
      val sourceList: util.List[util.Map[String, AnyRef]] = JSONUtils.fromJson(JSONUtils.toJson(map.get(Parameter.SOURCES)),
        new TypeToken[util.List[util.Map[String, AnyRef]]]() {}.getType)
      val sourceMap: util.Map[String, AnyRef] = sourceList.get(0)
      CheckHelper.checkNotNull(MapUtils.getString(sourceMap, Parameter.STORE_TYPE), Parameter.STORE_TYPE)
      val storeType: StoreType = StoreType.findStoreType(MapUtils.getString(sourceMap, Parameter.STORE_TYPE))
      var typeOfT: Type = null
      if (storeType == StoreType.KAFKA) {
        typeOfT = new TypeToken[KafkaSourceConfig]() {}.getType
        val kafka: KafkaSourceConfig = JSONUtils.fromJson(JSONUtils.toJson(sourceMap.get(Parameter.STORE_CONFIG)), typeOfT)
        Source(storeType, kafka)
      } else
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} source [$storeType] 类型不存在 ")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} ${ErrorCode.getJSON(e.getMessage)} <- getSource函数解析参数异常")
    }
  }

  /**
   * 根据storeType不同生成对应的dirtySink
   *
   * @param map 全局参数map
   * @return dirtySink
   */
  def getDirtyData(map: util.Map[String, AnyRef]): DirtyData = {
    try{
      val errorMap: util.Map[String, AnyRef] = JSONUtils.jsonToMap(JSONUtils.toJson(map.get(Parameter.ERROR_INFO)))
      val dirtyMap: util.Map[String, AnyRef] = JSONUtils.jsonToMap(JSONUtils.toJson(errorMap.get(Parameter.DIRTY_DATA)))
      CheckHelper.checkNotNull(MapUtils.getString(dirtyMap, Parameter.STORE_TYPE), Parameter.STORE_TYPE)
      val storeType: StoreType = StoreType.findStoreType(MapUtils.getString(dirtyMap, Parameter.STORE_TYPE))
      var typeOfT: Type = null
      if (storeType == StoreType.FILE)
        typeOfT = new TypeToken[FileConfig]() {}.getType
      else
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} dirtySink [$storeType] 类型不存在 ")

      CheckHelper.checkNotNull(MapUtils.getString(dirtyMap, Parameter.CONFIG), Parameter.CONFIG)
      val handleMode = dirtyMap.getOrDefault(Parameter.HANDLE_MODE, Long.box(-1L)).asInstanceOf[Long]
      val storeEnabled = dirtyMap.getOrDefault(Parameter.STORE_ENABLED, Boolean.box(false)).asInstanceOf[Boolean]
      val storeRowsLimit = dirtyMap.getOrDefault(Parameter.STORE_ROWS_LIMIT, Long.box(30000)).asInstanceOf[Long]
      val inboundTaskId = dirtyMap.get(Parameter.INBOUND_TASKID).asInstanceOf[String]
      val taskInstanceId = dirtyMap.get(Parameter.TASK_INSTANCEID).asInstanceOf[String]

      DirtyData(storeType, handleMode, storeEnabled, storeRowsLimit, inboundTaskId, taskInstanceId, JSONUtils.fromJson(JSONUtils.toJson(dirtyMap.get(Parameter.CONFIG)), typeOfT))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} ${ErrorCode.getJSON(e.getMessage)} <- getDirtyData函数解析参数异常")
    }
  }

  /**
   * 根据storeType不同生成对应的errorSink
   *
   * @param map 全局参数map
   * @return errorSink
   */
  def getLogInfo(map: util.Map[String, AnyRef]): LogInfo = {
    try{
      val errorMap: util.Map[String, AnyRef] = JSONUtils.jsonToMap(JSONUtils.toJson(map.get(Parameter.ERROR_INFO)))
      val logMap: util.Map[String, AnyRef] = JSONUtils.jsonToMap(JSONUtils.toJson(errorMap.get(Parameter.LOG_INFO)))
      CheckHelper.checkNotNull(MapUtils.getString(logMap, Parameter.STORE_TYPE), Parameter.STORE_TYPE)
      val storeType: StoreType = StoreType.findStoreType(MapUtils.getString(logMap, Parameter.STORE_TYPE))
      var typeOfT: Type = null
      if (storeType == StoreType.FILE)
        typeOfT = new TypeToken[FileConfig]() {}.getType
      else
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} errorSink [$storeType] 类型不存在 ")
      CheckHelper.checkNotNull(MapUtils.getString(logMap, Parameter.CONFIG), Parameter.CONFIG)
      val value:FileConfig = JSONUtils.fromJson(JSONUtils.toJson(logMap.get(Parameter.CONFIG)), typeOfT)
      LogInfo(storeType, value)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} ${ErrorCode.getJSON(e.getMessage)} <- getLogInfo函数解析参数异常")
    }
  }

  /**
   * 获取flinkConfig
   *
   * @param map 全局参数map
   * @return flinkConfig
   */
  def getFlinkConfig(map: util.Map[String, AnyRef]): FlinkConfig = {
    try{
      val flinkConfing: FlinkConfig = JSONUtils.fromJson(JSONUtils.toJson(map.get(Parameter.TASK_CONFIG)), new TypeToken[FlinkConfig]() {}.getType)
      flinkConfing.check
      flinkConfing
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} ${ErrorCode.getJSON(e.getMessage)} <- getFlinkConfig函数解析参数异常")
    }
  }


  /**
   * 获取sinks schema 的json字符串
   *
   * @param map 全局参数map
   * @return sparkConfig
   */
  def getSinks(map: util.Map[String, AnyRef]): String = {
    try {
      CheckHelper.checkSinks(map)
      CheckHelper.checkSchema(map)
      val sinks: Sinks = Sinks(map.get(Parameter.SINKS), map.get(Parameter.SCHEMA))
      JSONUtils.toJson(sinks)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK} ${ErrorCode.getJSON(e.getMessage)} <- getSinks函数解析参数异常")
    }
  }


  case class StreamingConfigHelper()

}
