package com.github.nnnnusui
package slideshow
package exo

case class Exo(header: Exo.Header, objects: Seq[TimelineObject]){
  def toExo: String
    = (header.toExo :: objects.zipWithIndex.map{case(obj, index)=> obj.toExo(index)}.toList).mkString("\n")
}
object Exo{
  case class Header(width: Int, height: Int, rate: Int
                   ,scale:     Int = 1
                   ,length:    Int = 1
                   ,audioRate: Int = 41000
                   ,audioCh:   Int = 2
                   ){
    def toExo: String
      =s"""[exedit]
          |width=$width
          |height=$height
          |rate=$rate
          |scale=$scale
          |length=$length
          |audio_rate=$audioRate
          |audio_ch=$audioCh""".stripMargin
  }
}
