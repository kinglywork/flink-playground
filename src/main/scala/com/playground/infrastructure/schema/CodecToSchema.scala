package com.playground.infrastructure.schema

import org.apache.avro.Schema
import vulcan.Codec

import java.io.{File, PrintWriter}

object CodecToSchema {
  private val schemaOutputDir = "src/main/avro"
  private val schemas = List(Codec[Canary].schema)

  def main(args: Array[String]): Unit =
    schemas.foreach {
      case Left(error) =>
        error.throwable.printStackTrace()
        System.out.println(s"Avro schema error: ${error.message}")
      case Right(schema) => generateSchemaFile(schema)
    }

  private def generateSchemaFile(schema: Schema): Unit = {
    System.out.println(s"Generating schema: ${schema.getName}")
    val namespaceDirectory = s"$schemaOutputDir/${schema.getNamespace}"
    createDirectory(namespaceDirectory)
    writeToFile(s"$namespaceDirectory/${schema.getName}.avsc", schema.toString(true))
  }

  private def createDirectory(namespaceDirectory: String): Unit = {
    val directory = new File(namespaceDirectory)
    if (!directory.exists()) {
      val _ = directory.mkdir()
    }
  }

  private def writeToFile(schemaPath: String, schemaString: String): Unit = {
    try {
      val avscWriter = new PrintWriter(new File(schemaPath))
      avscWriter.write(schemaString)
      avscWriter.close()
    } catch {
      case exception: Exception => print(s"Cannot write schema to file: ${exception.getMessage}")
    }
  }
}
