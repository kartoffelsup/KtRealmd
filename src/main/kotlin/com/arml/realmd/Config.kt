package com.arml.realmd

import com.google.common.base.CaseFormat
import com.google.common.base.Converter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KProperty

val camelCaseToUnderScore: Converter<String, String> =
  CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE)

class Config(configParser: ConfigParser) {
  val dbHost by configParser.stringProp()
  val dbPort by configParser.intProp()
  val dbPass by configParser.stringProp()
  val dbUser by configParser.stringProp()
  val dbName by configParser.stringProp()

  val listenAddress by configParser.stringProp()
  val listenPort by configParser.intProp()
}

class ConfigParser(private val configLocation: String, private val prefix: String) {
  private val props: Map<String, String> = loadProps()

  private fun loadProps(): Map<String, String> {
    val lines = Files.readAllLines(Paths.get(configLocation))
    return lines.asSequence()
      .filter { it.isNotBlank() }
      .map {
        val split = it.split('=')
        split[0] to split[1]
      }.toMap()
  }

  fun stringProp(): Delegate<String> {
    return Delegate(props, prefix) { it }
  }

  fun intProp(): Delegate<Int> {
    return Delegate(props, prefix) { it.toInt() }
  }
}

class Delegate<T>(
  private val props: Map<String, String>,
  private val prefix: String,
  private val transform: (String) -> T
) {
  operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
    val propPath = camelCaseToUnderScore.convert("$prefix.${prop.name}")?.replace('_', '.')
    val valueOrNull = props.filter { it.key == propPath }.values.firstOrNull()
    return valueOrNull?.let { transform(it) }
      ?: throw IllegalStateException("Property $propPath missing.")
  }
}
