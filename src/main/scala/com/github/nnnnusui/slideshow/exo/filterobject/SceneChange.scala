package com.github.nnnnusui
package slideshow
package exo.filterobject

import com.github.nnnnusui.slideshow.exo.FilterObject

case class SceneChange(adjustment: Double = 0.00
                       ,track1: Double = 0.00
                       ,flip: Double = 0
                       ,check0: Int = 0
                       ,_type: Int = 0
                       ,filter: Int = 0
                       ,name: String = ""
                       ,param: String = "*") extends FilterObject{
  def toExo: String
  =s"""_name=シーンチェンジ
      |調整=$adjustment
      |track1=$track1
      |反転=$flip
      |check0=$check0
      |type=${_type}
      |filter=$filter
      |name=$name
      |param=$param""".stripMargin
}
