package com.github.nnnnusui.slideshow

import java.nio.file.{Path, Paths}

import com.github.nnnnusui.slideshow.Exo.TimelineObject
import com.github.nnnnusui.slideshow.Timeline.Object.Picture
import javafx.scene.{input => jfxsi}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListCell, ListView, SelectionMode}
import scalafx.scene.input._

import scala.jdk.CollectionConverters._

class Timeline {
  val value = new ObservableBuffer[Picture]
  val valueLogger = new StateLogger(value.toVector)
  val view: ListView[Picture] = new ListView[Picture]{
    items.set(value)
    selectionModel.value.setSelectionMode(SelectionMode.Multiple)
    cellFactory = _=> new TimelineCell
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
  private def onDetect(targetIndex: Int = 0): Seq[Path] => Unit = paths =>{
    val pictures = paths.map(it=> Picture(it.toAbsolutePath))
    value.addAll(targetIndex, pictures.asJava)
    valueLogger.logging(value.toVector)
  }



  class TimelineCell extends ListCell[Picture] {
    item.onChange{ (_, _, value)=> text = if (value== null) "" else value.path.getFileName.toString }
    onMouseClicked = event=> if (event.getButton == jfxsi.MouseButton.MIDDLE) remove()
    onDragDetected = event=>{
      LocalSorting.onDetect(new MouseEvent(event))
      event.consume()
    }
    onDragOver = event=>{
      LocalSorting.onDragOver(new DragEvent(event))
      FilesDetector.onDragOver(new DragEvent(event))
      event.consume()
    }
    onDragEntered = _=> listView.get().getSelectionModel.clearAndSelect(index.value)
    onDragDropped = event=>{
      LocalSorting.onDragDropped(new DragEvent(event))
      FilesDetector.onDragDropped(new DragEvent(event), onDetect(Seq(value.size, index.value).min))
      event.consume()
    }
    private def remove(): Unit = {
      if(!empty.value)
        value.remove(index.value)
    }
    object LocalSorting{
      def onDetect(event: MouseEvent): Unit ={
        if (item == null) return
        val board = startDragAndDrop(TransferMode.Move)
        val content = new ClipboardContent()
        val items = listView.get().getSelectionModel.getSelectedItems.asScala
                            .map(_.toString).toSeq
        content.put(Timeline.dataFormat, items)
        board.setContent(content)
        /* remove */listView.get().getSelectionModel.getSelectedIndices.asScala.map(_.toInt).toSeq
                            .reverse.foreach(index=> value.remove(index))
      }
      def onDragOver(event: DragEvent): Unit ={
        if(!event.getDragboard.hasContent(Timeline.dataFormat)) return
        event.acceptTransferModes(TransferMode.Move)
      }
      def onDragDropped(event: DragEvent): Unit ={
        val board = event.getDragboard
        if (!board.hasContent(Timeline.dataFormat)) return
        val sources = board.getContent(Timeline.dataFormat).asInstanceOf[List[String]]
                           .map(str=> Picture(Paths.get(str)))
        val targetIndex = Seq(index.value, value.size).min
        value.addAll(targetIndex, sources.asJava)
        event.setDropCompleted(true)
        /* select */ val selectionModel = listView.get().getSelectionModel
          selectionModel.clearSelection()
          selectionModel.selectRange(targetIndex, targetIndex + sources.size)
        valueLogger.logging(value.toVector)
      }
    }
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
  sealed trait Object
  object Object{
    def toExoObjects(objects: List[Object], fps: Int, bpm: Int, step: Int): List[TimelineObject]
      = toExoObjects(objects, ChangedParameter(fps, bpm, step), 0, List(Exo.Object.FilterEffect.StandardDrawing()))
    def toExoObjects(objects: List[Object], parameter: ChangedParameter, count: Int, effects: List[Exo.Object.FilterEffect]): List[TimelineObject] ={
      import Exo.Object.MediaObject
      if (objects.isEmpty) return List.empty
      objects.head match {
//        case ParameterChange(bpm, step) =>
//          toExoObjects(objects.tail
//                      ,ChangedParameter(parameter.fps
//                                       ,bpm.getOrElse( parameter.bpm )
//                                       ,step.getOrElse(parameter.step))
//                      ,parameter.getNextStepCount(count)               )
        case Picture(path) =>
          val picture = MediaObject.Picture(path.toString)
          val start = parameter.getStartFrame(count)
          val end   = parameter.getEndFrame(count)
          val next  = parameter.getNextStepCount(count)
          val exoParameter = Exo.Object.Parameter(start, end)
          TimelineObject(exoParameter, picture, effects) :: toExoObjects(objects.tail, parameter, next, effects)
      }
    }
//    case class ParameterChange(bpm: Option[Int], step: Option[Int]) extends Object
    case class Picture(path: Path) extends Object {
      override def toString: String = path.toString
    }
//    case class Effect() extends Object
  }
}