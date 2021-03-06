package com.haizhi.volans.loader.scala.config.streaming

import com.haizhi.volans.loader.scala.config.schema.SchemaVo
import com.haizhi.volans.loader.scala.config.streaming.error.ErrorInfo
import com.haizhi.volans.loader.scala.config.streaming.flink.FlinkConfig
import com.haizhi.volans.loader.scala.config.streaming.source.{KafkaSourceConfig, Source}

/**
 * 全局参数配置类
 * sinks = sinks和schema合在一起的json字符串，用于sinks模块调用
 */
case class StreamingConfig(source: Source,
                           sinks: String,
                           schemaVo: SchemaVo,
                           errorInfo: ErrorInfo,
                           flinkConfig: FlinkConfig) {

}
