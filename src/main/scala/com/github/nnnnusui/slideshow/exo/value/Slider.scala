package com.github.nnnnusui.slideshow.exo.value

case class Slider(from: Double, to: Double = 0.0, mode: Int = 0){
  override def toString: String
    = mode match {
        case 0 => s"$from"
        case _ => s"$from,$to,$mode"
      }
}
