package com.github.nnnnusui
package slideshow

import java.nio.file.Path

import com.github.nnnnusui.slideshow.Timeline.Object.Picture
import com.github.nnnnusui.slideshow.exo.TimelineObject
import com.github.nnnnusui.slideshow.exo.TimelineObject.Parameter
import com.github.nnnnusui.slideshow.exo.filtereffect.StandardDrawing
import javafx.scene.{input => jfxsi}
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.{ListView, SelectionMode}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input._
import scalafx.scene.layout.{BorderPane, Pane}

import scala.jdk.CollectionConverters._

class Timeline {
  val value = new ObservableBuffer[Timeline.Object]
  val valueLogger = new StateLogger(value.toVector)
  val listView: ListView[Timeline.Object] = new ListView[Timeline.Object]{
    items.set(value)
    selectionModel.value.setSelectionMode(SelectionMode.Multiple)
    onDragOver    = event=>{ FilesDetector.onDragOver(new DragEvent(event));                event.consume() }
    onDragDropped = event=>{ FilesDetector.onDragDropped(new DragEvent(event), onDetect()); event.consume() }
    onKeyPressed = event=> {
      event.getCode match {
        case it if it == jfxsi.KeyCode.Z && event.isControlDown
          => value.setAll((if (event.isShiftDown) valueLogger.redo()
                           else                   valueLogger.undo()).asJava)
        case _ =>
      }
    }
  }
  val view = new BorderPane{
    center = listView
  }
  listView.cellFactory = _=> new TimelineCell(this)
  var preview = new BorderPane
  def onDetect(targetIndex: Int = 0): Seq[Path] => Unit = paths =>{
    val pictures = paths.map(it=> Picture(it.toAbsolutePath))
    value.addAll(targetIndex, pictures.asJava)
    valueLogger.logging(value.toVector)
  }
}

object Timeline{
  val dataFormat = new DataFormat("com.github.nnnnusui.slideshow.Timeline.TimelineCell")
  case class ChangedParameter(fps: Int, bpm: Int, step: Int){
    val framePerBeat: Double = (fps * 60) / bpm.toDouble
    def getStartFrame(count: Int): Int = (framePerBeat * count).toInt +1
    def getEndFrame(count: Int): Int = (framePerBeat * (count+step)).toInt
    def getNextStepCount(current: Int): Int = current + step
  }
  sealed trait Object {
    val name: String
    def getView(pa: Pane): Node
  }
  object Object{
    def toExoObjects(objects: List[Object], fps: Int, bpm: Int, step: Int): List[TimelineObject]
      = toExoObjects(objects, ChangedParameter(fps, bpm, step), 0, List(StandardDrawing()))
    def toExoObjects(objects: List[Object], parameter: ChangedParameter, count: Int, effects: List[exo.FilterEffect]): List[TimelineObject] ={
      if (objects.isEmpty) return List.empty
      objects.head match {
//        case ParameterChange(bpm, step) =>
//          toExoObjects(objects.tail
//                      ,ChangedParameter(parameter.fps
//                                       ,bpm.getOrElse( parameter.bpm )
//                                       ,step.getOrElse(parameter.step))
//                      ,parameter.getNextStepCount(count)               )
        case Picture(path) =>
          val picture = exo.mediaobject.Picture(path.toString)
          val start = parameter.getStartFrame(count)
          val end   = parameter.getEndFrame(count)
          val next  = parameter.getNextStepCount(count)
          val exoParameter = Parameter(start, end)
          TimelineObject(exoParameter, picture, effects) :: toExoObjects(objects.tail, parameter, next, effects)
      }
    }
//    case class ParameterChange(bpm: Option[Int], step: Option[Int]) extends Object
    case class Picture(path: Path) extends Object {
      override def toString: String = path.toString
      val name: String = path.getFileName.toString
      def getView(pa: Pane): Node = new ImageView{
        preserveRatio = true
        minWidth(0)
        minHeight(0)
        Platform.runLater{()=> image = new Image(path.toUri.toString) }
        fitWidth  <== pa.width
        fitHeight <== pa.height
      }
    }
    class Exo{
      override def toString: String
        =s"""
            |
            |""".stripMargin
    }
  }
}