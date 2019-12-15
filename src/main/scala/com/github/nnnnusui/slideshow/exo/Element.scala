package com.github.nnnnusui
package slideshow
package exo

sealed trait Element{ def toExo: String }
trait FilterEffect extends Element
trait FilterObject extends Element
trait MediaObject extends Element