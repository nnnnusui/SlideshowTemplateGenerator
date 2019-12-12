package com.github.nnnnusui.slideshow

import com.github.nnnnusui.slideshow.EXO.Object.{MediaObject, Parameter, TimelineObject}

import scala.io.StdIn

object TemplateGenerator extends App {
  println(EXO.Header(1280, 720, 30).toExo)
  generate(128, 30, 1)

  def generate(bpm: Int, fps: Int, step: Int): Unit ={
    val framePerBeat = (fps * 60) / bpm.toDouble
    (0 to 100 by step).zipWithIndex.foreach{ case (barIndex, index)=>
      val parameter = {
        val start = (framePerBeat * barIndex).toInt + 1
        val end   = (framePerBeat * (barIndex + step)).toInt
        Parameter(start, end)
      }
      val picture   = MediaObject.Picture(s"G:\\workspace\\movie\\AviUtl\\$index.png")
      val obj = TimelineObject(index, parameter, picture)
      println(obj.toExo)
    }
  }
}

object EXO{
  case class Header(width: Int, height: Int, rate: Int
                   ,scale:     Int = 1
                   ,length:    Int = 1
                   ,audioRate: Int = 41000
                   ,audioCh:   Int = 2) extends Object{
    override def toExo: String
      =s"""[exedit]
          |width=$width
          |height=$height
          |rate=$rate
          |scale=$scale
          |length=$length
          |audio_rate=$audioRate
          |audio_ch=$audioCh""".stripMargin
  }

  sealed trait Object{ def toExo: String }
  object Object{
    case class TimelineObject(index: Int, parameter: Parameter, obj: MediaObject) extends Object{
      def toExo: String
        =s"""[$index]
            |${parameter.toExo}
            |[$index.0]
            |${obj.toExo}""".stripMargin
    }
    case class Parameter(start: Int, end: Int
                        ,layer:   Int = 1
                        ,overlay: Int = 1
                        ,camera:  Int = 0) extends Object{
      def toExo: String
        =s"""start=$start
            |end=$end
            |layer=$layer
            |overlay=$overlay
            |camera=$camera""".stripMargin
    }
    sealed trait MediaObject extends Object
    object MediaObject{
      case class Picture(filePath: String) extends MediaObject{
        def toExo: String
          =s"""_name=画像ファイル
              |file=$filePath""".stripMargin
      }
    }
  }
}
