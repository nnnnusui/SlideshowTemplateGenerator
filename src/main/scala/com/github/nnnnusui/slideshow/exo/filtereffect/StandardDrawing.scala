package com.github.nnnnusui
package slideshow
package exo.filtereffect

import com.github.nnnnusui.slideshow.exo.FilterEffect

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
