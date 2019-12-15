package com.github.nnnnusui.slideshow

import java.nio.file.{Path, Paths}

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
  sealed trait Object
  object Object{
    def toExoObjects(objects: List[Object]): List[Exo.Object.MediaObject] ={
      if (objects.isEmpty) return List.empty
      (objects.head match {
        case Picture(path) => Exo.Object.MediaObject.Picture(path.toString)
      }) :: toExoObjects(objects.tail)
    }
    case class Picture(path: Path) extends Object {
      override def toString: String = path.toString
    }
  }
}