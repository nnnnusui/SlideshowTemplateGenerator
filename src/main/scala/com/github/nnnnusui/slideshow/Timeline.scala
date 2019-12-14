package com.github.nnnnusui.slideshow

import java.nio.file.{Files, Path, Paths}

import com.github.nnnnusui.slideshow.Timeline.Picture
import javafx.scene.{input => jfxsi}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListCell, ListView, SelectionMode}
import scalafx.scene.input.{ClipboardContent, DataFormat, DragEvent, Dragboard, MouseEvent, TransferMode}

import scala.jdk.CollectionConverters._

class Timeline {
  var timelineLog: List[Vector[Picture]] = List(Vector.empty[Picture])
  var logAccessIndex = 0
  val value = new ObservableBuffer[Picture]
  val view: ListView[Picture] = new ListView[Picture]{
    items.set(value)
    selectionModel.value.setSelectionMode(SelectionMode.Multiple)
    cellFactory = _=> new TimelineCell
    onDragOver    = event=>{ FilesDetector.onDragOver(new DragEvent(event));    event.consume() }
    onDragDropped = event=>{ FilesDetector.onDragDropped(new DragEvent(event)); event.consume() }

    onKeyPressed = event=> {
      event.getCode match {
        case it if it == jfxsi.KeyCode.Z && event.isControlDown && event.isShiftDown => redo()
        case it if it == jfxsi.KeyCode.Z && event.isControlDown => undo()
        case _ =>
      }
    }
  }
  def logging(): Unit ={
    val log = timelineLog.drop(logAccessIndex)
    timelineLog = value.toVector :: log.take(9)
    logAccessIndex = 0
    println(timelineLog.map(_.size))
  }
  def undo(): Unit ={
    if(!(logAccessIndex +1 < timelineLog.size)) return
    logAccessIndex += 1
    value.setAll(timelineLog(logAccessIndex).asJava)
    println(timelineLog.map(_.size) +" "+ logAccessIndex)
  }
  def redo(): Unit = {
    if(!(logAccessIndex > 0)) return
    logAccessIndex -= 1
    value.setAll(timelineLog(logAccessIndex).asJava)
    println(timelineLog.map(_.size) +" "+ logAccessIndex)
  }

  object FilesDetector{
    def onDragOver(event: DragEvent): Unit ={
      if(!event.getDragboard.hasFiles) return
      event.acceptTransferModes(TransferMode.Link)
    }
    def onDragDropped(event: DragEvent, index: Int = 0): Unit ={
      import scala.jdk.CollectionConverters._
      val board = new Dragboard(event.getDragboard)
      if (!board.hasFiles) return
      val pictures = walkFiles(board).map(it=> Picture(it.toAbsolutePath))
      value.addAll(index, pictures.asJava)
      event.setDropCompleted(true)
      logging()
    }
    private def walkFiles(dragboard: Dragboard): Seq[Path] ={
      dragboard.getFiles.asScala
        .map(_.toPath)
        .flatMap { it =>
          if (Files.isDirectory(it))
            Files.walk(it).filter(it => !Files.isDirectory(it))
              .iterator().asScala.toSeq
          else Seq(it)
        }.toSeq
    }
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
      FilesDetector.onDragDropped(new DragEvent(event), Seq(value.size, index.value).min)
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
        logging()
      }
    }
  }
}

object Timeline{
  val dataFormat = new DataFormat("com.github.nnnnusui.slideshow.Timeline.TimelineCell")
  case class Picture(path: Path){
    override def toString: String = path.toString
  }
}