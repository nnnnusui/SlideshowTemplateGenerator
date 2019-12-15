package com.github.nnnnusui
package slideshow
package exo

import exo.TimelineObject.Parameter

case class TimelineObject(parameter: Parameter, obj: Element
                          ,effects: List[FilterEffect] = List.empty
                         ) {
  def toExo(index: Int): String = {
    val objectsStr = (obj :: effects).zipWithIndex.flatMap { case (it, itIndex) =>
      List(s"[$index.$itIndex]", it.toExo)
    }.mkString("\n")
    s"""[$index]
       |${parameter.toExo(obj)}
       |$objectsStr""".stripMargin
  }
}
object TimelineObject{
  case class Parameter(start: Int, end: Int
                       ,layer:   Int = 1
                       ,overlay: Int = 1
                       ,camera:  Int = 0){
    def toExo(obj: Element): String = {
      val x = obj match {
        case _: MediaObject  =>s"\noverlay=$overlay\ncamera=$camera"
        case _: FilterObject =>""
      }
      s"""start=$start
         |end=$end
         |layer=$layer""".stripMargin + s"$x"
    }
  }
}
