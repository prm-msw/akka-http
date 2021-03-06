/*
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.http.impl.settings

import akka.annotation.InternalApi
import akka.http.scaladsl.settings.ParserSettings.{ CookieParsingMode, ErrorLoggingVerbosity, IllegalResponseHeaderValueProcessingMode }
import akka.util.ConstantFun
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import akka.http.scaladsl.model._
import akka.http.impl.util._

/** INTERNAL API */
@InternalApi
private[akka] final case class ParserSettingsImpl(
  maxUriLength:                             Int,
  maxMethodLength:                          Int,
  maxResponseReasonLength:                  Int,
  maxHeaderNameLength:                      Int,
  maxHeaderValueLength:                     Int,
  maxHeaderCount:                           Int,
  maxContentLength:                         Long,
  maxChunkExtLength:                        Int,
  maxChunkSize:                             Int,
  uriParsingMode:                           Uri.ParsingMode,
  cookieParsingMode:                        CookieParsingMode,
  illegalHeaderWarnings:                    Boolean,
  ignoreIllegalHeaderFor:                   Set[String],
  errorLoggingVerbosity:                    ErrorLoggingVerbosity,
  illegalResponseHeaderValueProcessingMode: IllegalResponseHeaderValueProcessingMode,
  headerValueCacheLimits:                   Map[String, Int],
  includeTlsSessionInfoHeader:              Boolean,
  modeledHeaderParsing:                     Boolean,
  customMethods:                            String ⇒ Option[HttpMethod],
  customStatusCodes:                        Int ⇒ Option[StatusCode],
  customMediaTypes:                         MediaTypes.FindCustom)
  extends akka.http.scaladsl.settings.ParserSettings {

  require(maxUriLength > 0, "max-uri-length must be > 0")
  require(maxMethodLength > 0, "max-method-length must be > 0")
  require(maxResponseReasonLength > 0, "max-response-reason-length must be > 0")
  require(maxHeaderNameLength > 0, "max-header-name-length must be > 0")
  require(maxHeaderValueLength > 0, "max-header-value-length must be > 0")
  require(maxHeaderCount > 0, "max-header-count must be > 0")
  require(maxContentLength > 0, "max-content-length must be > 0")
  require(maxChunkExtLength > 0, "max-chunk-ext-length must be > 0")
  require(maxChunkSize > 0, "max-chunk-size must be > 0")

  override val defaultHeaderValueCacheLimit: Int = headerValueCacheLimits("default")

  override def headerValueCacheLimit(headerName: String): Int =
    headerValueCacheLimits.getOrElse(headerName, defaultHeaderValueCacheLimit)

  override def productPrefix = "ParserSettings"

  // optimization: if we see the default value as defined below, we know it hasn't been changed
  override def areNoCustomMediaTypesDefined: Boolean = customMediaTypes eq ParserSettingsImpl.noCustomMediaTypes
}

object ParserSettingsImpl extends SettingsCompanion[ParserSettingsImpl]("akka.http.parsing") {

  private[this] val noCustomMethods: String ⇒ Option[HttpMethod] = ConstantFun.scalaAnyToNone
  private[this] val noCustomStatusCodes: Int ⇒ Option[StatusCode] = ConstantFun.scalaAnyToNone
  private[ParserSettingsImpl] val noCustomMediaTypes: (String, String) ⇒ Option[MediaType] = ConstantFun.scalaAnyTwoToNone

  def fromSubConfig(root: Config, inner: Config) = {
    val c = inner.withFallback(root.getConfig(prefix))
    val cacheConfig = c getConfig "header-cache"

    new ParserSettingsImpl(
      c getIntBytes "max-uri-length",
      c getIntBytes "max-method-length",
      c getIntBytes "max-response-reason-length",
      c getIntBytes "max-header-name-length",
      c getIntBytes "max-header-value-length",
      c getIntBytes "max-header-count",
      c getPossiblyInfiniteBytes "max-content-length",
      c getIntBytes "max-chunk-ext-length",
      c getIntBytes "max-chunk-size",
      Uri.ParsingMode(c getString "uri-parsing-mode"),
      CookieParsingMode(c getString "cookie-parsing-mode"),
      c getBoolean "illegal-header-warnings",
      (c getStringList "ignore-illegal-header-for").asScala.map(_.toLowerCase).toSet,
      ErrorLoggingVerbosity(c getString "error-logging-verbosity"),
      IllegalResponseHeaderValueProcessingMode(c getString "illegal-response-header-value-processing-mode"),
      cacheConfig.entrySet.asScala.map(kvp ⇒ kvp.getKey → cacheConfig.getInt(kvp.getKey))(collection.breakOut),
      c getBoolean "tls-session-info-header",
      c getBoolean "modeled-header-parsing",
      noCustomMethods,
      noCustomStatusCodes,
      noCustomMediaTypes)
  }

}

