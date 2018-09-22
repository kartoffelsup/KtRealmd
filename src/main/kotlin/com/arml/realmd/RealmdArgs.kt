package com.arml.realmd

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class RealmdArgs(argParser: ArgParser) {
  val configLocation by argParser.storing(
    "-c",
    "--config",
    help = "The '.properties' file which contains configuration for realmd."
  ).default("realmd.properties")
}
