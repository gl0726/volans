package com.haizhi.volans.sink.component

import java.util

import com.arangodb.model.DocumentImportOptions
import com.arangodb.model.DocumentImportOptions.OnDuplicate
import com.haizhi.volans.common.flink.base.scala.util.JSONUtils
import com.haizhi.volans.sink.config.constant.{CoreConstants, Keys, OperationMode, StoreType}
import com.haizhi.volans.sink.server.AtlasDao
import com.haizhi.volans.sink.config.schema.SchemaVo
import com.haizhi.volans.sink.config.store.StoreAtlasConfig
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.slf4j.LoggerFactory

/**
 * Author pengxb
 * Date 2020/11/16
 */
class ArangoDBSink(override var storeType: StoreType,
                   var storeConfig: StoreAtlasConfig,
                   var schemaVo: SchemaVo)
  extends RichSinkFunction[Iterable[String]] with Sink {

  private val logger = LoggerFactory.getLogger(classOf[ArangoDBSink])
  override var uid: String = "ArangoDB"
  private val atlasDao = new AtlasDao()
  private var operationMode: OperationMode = _

  /**
   * 初始化
   *
   * @param parameters
   */
  override def open(parameters: Configuration): Unit = {
    storeConfig.collectionType = schemaVo.`type`
    atlasDao.initClient(storeConfig)
    atlasDao.createTableIfNecessary(storeConfig)
    this.operationMode = OperationMode.findStoreType(schemaVo.operation.mode)
  }

  /**
   * 处理数据
   *
   * @param elements
   * @param context
   */
  override def invoke(elements: Iterable[String], context: SinkFunction.Context[_]): Unit = {
    // 删除标识
    var deleteFlag = operationMode == OperationMode.DELETE
    val filteredTuple = elements.map(record => {
      val recordMap = JSONUtils.jsonToJavaMap(record)
      validateAndMerge(recordMap)
      if (operationMode == OperationMode.MIX) {
        val operationValue = recordMap.get(schemaVo.operation.operateField)
        if (operationValue != null) {
          deleteFlag = recordMap.get(schemaVo.operation) != null && CoreConstants.OPERATION_DELETE.equalsIgnoreCase(operationValue.toString)
          recordMap.remove(schemaVo.operation.operateField)
        }
      }
      (JSONUtils.toJson(recordMap), deleteFlag)
    }
    ).toList

    // Delete List
    val deleteList = filteredTuple.filter(_._2).map(_._1)
    logger.debug(s"arango delete list: $deleteList")
    if (deleteList != null && deleteList.size > 0) {
      val col = atlasDao.getArangoDB().db(storeConfig.database).collection(storeConfig.collection)
      atlasDao.deleteArango(deleteList, col, storeConfig.importBatchSize)
    }
    // Upsert List
    val upsertList = filteredTuple.filter(!_._2).map(_._1)
    logger.debug(s"arango delete list: $upsertList")
    if (upsertList != null && upsertList.size > 0) {
      val col = atlasDao.getArangoDB().db(storeConfig.database).collection(storeConfig.collection)
      atlasDao.updateArango(upsertList, col, new DocumentImportOptions().onDuplicate(OnDuplicate.update), storeConfig.importBatchSize)
    }

  }

  /**
   * 参数校验、转换
   *
   * @param element
   */
  def validateAndMerge(element: util.Map[String, Object]): Unit = {
    if (element.containsKey(Keys.OBJECT_KEY) && !element.containsKey(Keys._KEY)) {
      element.put(Keys._KEY, element.get(Keys.OBJECT_KEY))
    }
    if (schemaVo.isEdge()) {
      if (element.containsKey(Keys.FROM_KEY) && !element.containsKey(Keys._FROM)) {
        element.put(Keys._FROM, element.get(Keys.FROM_KEY))
      }
      if (element.containsKey(Keys.TO_KEY) && !element.containsKey(Keys._TO)) {
        element.put(Keys._TO, element.get(Keys.TO_KEY))
      }
    }
  }

  override def build[T](v: T): RichSinkFunction[T] = {
    this.asInstanceOf[RichSinkFunction[T]]
  }

  /**
   * 释放资源
   */
  override def close(): Unit = {
    atlasDao.shutdown()
  }

}
