package com.haizhi.volans.loader.scala.config.check

import java.util
import com.google.gson.reflect.TypeToken
import com.haizhi.volans.common.flink.base.scala.exception.ErrorCode
import com.haizhi.volans.common.flink.base.scala.util.JSONUtils
import com.haizhi.volans.loader.scala.config.exception.VolansCheckException
import com.haizhi.volans.loader.scala.config.parameter.{Parameter, SinksParameter}
import com.haizhi.volans.loader.scala.config.schema.Keys
import com.hzxt.volans.loader.java.StoreType
import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import scala.collection.mutable

/**
 * 检查入参
 *
 * @author gl 
 **/
object CheckHelper {

  private val LOG: Logger =  LoggerFactory.getLogger(classOf[CheckHelper])

  /**
   * 检查map中是否有不存在的key
   */
  def checkMap(map: util.Map[String, AnyRef]): Unit = {
    if (!map.containsKey(Parameter.SOURCE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.SOURCE}] Key field missing")
    if (!map.containsKey(Parameter.SINKS))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.SINKS}] Key field missing")
    if (!map.containsKey(Parameter.SCHEMA))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.SCHEMA}] Key field missing")
    if (!map.containsKey(Parameter.DIRTY_SINK))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.DIRTY_SINK}] Key field missing")
    if (!map.containsKey(Parameter.ERROR_SINK))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.ERROR_SINK}] Key field missing")
    if (!map.containsKey(Parameter.CHECKPOINT))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.CHECKPOINT}] Key field missing")
    if (!map.containsKey(Parameter.FLINK_CONFIG))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.FLINK_CONFIG}] Key field missing")
    LOG.info(" checkMap The parameters are correct")
  }

  def checkGDB(map: util.Map[String, AnyRef]): Unit = {
    LOG.info(s" checkGDB  map : $map")
    if (!map.containsKey(SinksParameter.URL))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   GBD sink  [${SinksParameter.URL}] Key field missing")
    if (!map.containsKey(SinksParameter.USER))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   GBD sink  [${SinksParameter.USER}] Key field missing")
    if (!map.containsKey(SinksParameter.PASSWORD))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   GBD sink  [${SinksParameter.PASSWORD}] Key field missing")
    if (!map.containsKey(SinksParameter.DATABASE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   GBD sink  [${SinksParameter.DATABASE}] Key field missing")
    if (!map.containsKey(SinksParameter.COLLECTION))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   GBD sink  [${SinksParameter.COLLECTION}] Key field missing")
    LOG.info(" checkGDB The parameters are correct")
  }

  def checkJANUS(map: util.Map[String, AnyRef]): Unit = {
    LOG.info(s" checkJANUS map : $map")
    if (!map.containsKey(SinksParameter.DATABASE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   JANUS sink  [${SinksParameter.DATABASE}] Key field missing")
    LOG.info(" checkJANUS The parameters are correct")
  }

  def checkHive(map: util.Map[String, AnyRef]): Unit = {
    LOG.info(s" checkHive map : $map")
    if (!map.containsKey(SinksParameter.DATABASE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   HIVE sink  [${SinksParameter.DATABASE}] Key field missing")
    if (!map.containsKey(SinksParameter.TABLE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   HIVE sink  [${SinksParameter.TABLE}] Key field missing")
    if (!map.containsKey(SinksParameter.DEL_FIELD))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   HIVE sink  [${SinksParameter.DEL_FIELD}] Key field missing")
    LOG.info(" checkHive The parameters are correct")
  }

  def checkES(map: util.Map[String, AnyRef]): Unit = {
    LOG.info(s" checkES map : $map")
    if (!map.containsKey(SinksParameter.URL))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   ES sink  [${SinksParameter.URL}] Key field missing")
    if (!map.containsKey(SinksParameter.INDEX))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   ES sink  [${SinksParameter.INDEX}] Key field missing")
    if (!map.containsKey(SinksParameter.TYPE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   ES sink  [${SinksParameter.TYPE}] Key field missing")
    LOG.info(" checkES The parameters are correct")
  }

  def checkHBASE(map: util.Map[String, AnyRef]): Unit = {
    LOG.info(s" checkHBASE map : $map")
    if (!map.containsKey(SinksParameter.URL))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   HBASE sink  [${SinksParameter.URL}] Key field missing")
    if (!map.containsKey(SinksParameter.TABLE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   HBASE sink  [${SinksParameter.INDEX}] Key field missing")
    LOG.info(" checkHBASE The parameters are correct")
  }

  /**
   * 此函数用于检查sinks中各个sink中关键parameters是否具备
   * @return
   */
  def checkSinks(map: util.Map[String, AnyRef]): Unit = {
    val sinks: AnyRef = map.get(Parameter.SINKS)
    val sinksMaps: util.List[util.Map[String, AnyRef]] = JSONUtils.fromJson(JSONUtils.toJson(sinks), new TypeToken[util.List[util.Map[String, AnyRef]]]() {}.getType)
    val affected_store = new mutable.StringBuilder()
    for (index <- 0 until sinksMaps.size()) {
      val sinkMap: util.Map[String, AnyRef] = sinksMaps.get(index)
      //检查关键parameters
      checkNotNull(MapUtils.getString(sinkMap, Parameter.STORE_TYPE), Parameter.STORE_TYPE, taskId = Keys.taskInstanceId)
      checkNotNull(MapUtils.getString(sinkMap, Parameter.STORE_CONFIG), Parameter.STORE_CONFIG, taskId = Keys.taskInstanceId)
      val storeType: StoreType = StoreType.findStoreType(MapUtils.getString(sinkMap, Parameter.STORE_TYPE))
      if (storeType == StoreType.GDB || storeType == StoreType.ATLAS) {
        checkGDB(JSONUtils.jsonToMap(JSONUtils.toJson(sinkMap.get(Parameter.STORE_CONFIG))))
        affected_store.append("GDB,")
      } else if (storeType == StoreType.JANUS) {
        checkJANUS(JSONUtils.jsonToMap(JSONUtils.toJson(sinkMap.get(Parameter.STORE_CONFIG))))
        affected_store.append("JANUS,")
      } else if (storeType == StoreType.HBASE) {
        checkHBASE(JSONUtils.jsonToMap(JSONUtils.toJson(sinkMap.get(Parameter.STORE_CONFIG))))
        affected_store.append("HBASE,")
      } else if (storeType == StoreType.ES) {
        checkES(JSONUtils.jsonToMap(JSONUtils.toJson(sinkMap.get(Parameter.STORE_CONFIG))))
        affected_store.append("ES,")
      } else if (storeType == StoreType.HIVE) {
        checkHive(JSONUtils.jsonToMap(JSONUtils.toJson(sinkMap.get(Parameter.STORE_CONFIG))))
        affected_store.append("HIVE,")
      } else
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}  parameters  [${Parameter.SINKS}] storeType 类型不存在")
    }
    Keys.affected_store = affected_store.deleteCharAt(affected_store.length - 1).toString()
    LOG.info(s" info sinks affected_store = ${Keys.affected_store} ")
    LOG.info(" checkSinks The parameters are correct")
  }

  /**
   * 检查schema parameters
   */
  def checkSchema(map: util.Map[String, AnyRef]): Unit = {
    val schema: AnyRef = map.get(Parameter.SCHEMA)
    val schemaMap: util.Map[String, AnyRef] = JSONUtils.jsonToMap(JSONUtils.toJson(schema))
    if (!schemaMap.containsKey(Parameter.FIELDS))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   schema  [${Parameter.FIELDS}] Key field missing")
    if (!schemaMap.containsKey(Parameter.NAME))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   schema  [${Parameter.NAME}] Key field missing")
    if (!schemaMap.containsKey(Parameter.TYPE))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   schema  [${Parameter.TYPE}] Key field missing")
    //检查插入类型：点或边
    if (!Keys.VERTEX.equalsIgnoreCase(schemaMap.get(Parameter.TYPE).toString) && !Keys.EDGE.equalsIgnoreCase(schemaMap.get(Parameter.TYPE).toString))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   schema  [${Parameter.TYPE}:${schemaMap.get(Parameter.TYPE)}] 字段类型异常，正确类型 [vertex, edge]")
    //检查fileds字段
    val fileds: AnyRef = schemaMap.get(Parameter.FIELDS)
    val filedsMap: util.Map[String, AnyRef] = JSONUtils.jsonToMap(JSONUtils.toJson(fileds))
    if (filedsMap.size() == 0)
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   The schema [filedsMap] length is zero")
    //循环获取每个filed字段中是否包含name , type
    val value: util.Iterator[String] = filedsMap.keySet.iterator()
    while (value.hasNext) {
      val key: String = value.next()
      val map1: mutable.Map[String, AnyRef] = JSONUtils.jsonToScalaMap(JSONUtils.toJson(filedsMap.get(key)))
      if (!map1.contains(Parameter.NAME))
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   schema fileds [$key:${Parameter.NAME}] Key field missing")
      if (!map1.contains(Parameter.TYPE))
        throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   schema fileds [$key:${Parameter.TYPE}] Key field missing")
    }
    LOG.info(" schema fileds The parameters are correct")
  }

  /**
   * 检查关键parameters是否为空
   */
  def checkNotNull(value: String, `type`: String, taskId: String = ""): Unit = {
    if (StringUtils.isBlank(value))
      throw new VolansCheckException(s"${ErrorCode.PARAMETER_CHECK_ERROR}${ErrorCode.PATH_BREAK}   parameters ${`type`} is null")
  }

  case class CheckHelper()

}
