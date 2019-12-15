package com.github.nnnnusui
package slideshow

import com.github.nnnnusui.slideshow.Exo.Object.{MediaObject, Parameter}

case class Exo(header: Exo.Header, objects: Seq[Exo.TimelineObject]){
  def toExo: String
    = (header.toExo :: objects.zipWithIndex.map{case(obj, index)=> obj.toExo(index)}.toList).mkString("\n")
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
  case class TimelineObject(parameter: Parameter, obj: MediaObject
                           ,effects: List[Object.FilterEffect] = List.empty
                           ){
    def toExo(index: Int): String = {
      val objectsStr = (obj :: effects).zipWithIndex.flatMap{case(it, itIndex)=>
        List(s"[$index.$itIndex]", it.toExo)
      }.mkString("\n")
      s"""[$index]
         |${parameter.toExo}
         |$objectsStr""".stripMargin
    }
  }

  sealed trait Object{ def toExo: String }
  object Object{
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
      case class StandardDrawing(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0
                                 ,magnification: Double = 100.00
                                 ,transparency:  Double = 0.0
                                 ,rotation: Double = 0.00
                                 ,blend: Int = 0) extends FilterEffect {
        def toExo: String
        =s"""_name=標準描画
            |X=$x
            |Y=$y
            |Z=$z
            |拡大率=$magnification
            |透明度=$transparency
            |回転=$rotation
            |blend=$blend""".stripMargin
      }
    }
  }
}
