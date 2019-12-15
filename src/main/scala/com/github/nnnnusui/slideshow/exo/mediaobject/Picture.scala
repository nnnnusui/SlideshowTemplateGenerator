package com.github.nnnnusui.slideshow.exo.mediaobject

import com.github.nnnnusui.slideshow.exo.MediaObject

case class Picture(filePath: String) extends MediaObject{
  def toExo: String
  =s"""_name=画像ファイル
      |file=$filePath""".stripMargin
}
