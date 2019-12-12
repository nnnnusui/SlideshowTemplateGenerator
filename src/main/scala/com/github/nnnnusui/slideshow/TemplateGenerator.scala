package com.github.nnnnusui.slideshow

import java.nio.file.Path

import com.github.nnnnusui.slideshow.Exo.Object.{MediaObject, Parameter, TimelineObject}

import scala.io.StdIn

object TemplateGenerator extends App {
//  val header = Exo.Header(1280, 720, 30)
//  val objects = generate(128, 30, 1, Seq())
//  println(Exo(header, objects).toExo)

  def generate(bpm: Int, fps: Int, step: Int, objects: Seq[MediaObject]): Seq[TimelineObject] ={
    val framePerBeat = (fps * 60) / bpm.toDouble
    val maxFrame = objects.size * step
    objects.zipWithIndex.map{ case (obj, index)=>
      val barCount = index * step
      val parameter = {
        val start = (framePerBeat * barCount).toInt + 1
        val end   = (framePerBeat * (barCount + step)).toInt
        Parameter(start, end)
      }
      TimelineObject(index, parameter, obj)
    }
  }
}

case class Exo(header: Exo.Header, objects: Seq[Exo.Object.TimelineObject]){
  def toExo: String
    = (header :: objects.toList).map(_.toExo).mkString("\n")
}
object Exo{
  case class Header(width: Int, height: Int, rate: Int
                   ,scale:     Int = 1
                   ,length:    Int = 1
                   ,audioRate: Int = 41000
                   ,audioCh:   Int = 2
                   ) extends Object{
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
    case class TimelineObject(index: Int, parameter: Parameter, obj: MediaObject
                             //,effects: Seq[FilterEffect] = Seq.empty
                             ) extends Object{
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
    sealed trait FilterEffect extends Object
    object FilterEffect{

    }
  }
}