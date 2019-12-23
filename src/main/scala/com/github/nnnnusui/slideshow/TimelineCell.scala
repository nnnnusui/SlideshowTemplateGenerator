package com.github.nnnnusui.slideshow

import java.nio.file.Paths

import com.github.nnnnusui.slideshow.Timeline.Object.Picture
import javafx.scene.{input => jfxsi}
import scalafx.scene.control.ListCell
import scalafx.scene.input.{ClipboardContent, DragEvent, MouseEvent, TransferMode}

import scala.jdk.CollectionConverters._

class TimelineCell(val tl: Timeline) extends ListCell[Timeline.Object] {
  item.onChange{ (_, _, value)=> text = if (value == null) "" else value.name }
  selected.onChange { (_, _, value) => if (value) tl.preview.center = item.value.getView(tl.preview) }
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
    FilesDetector.onDragDropped(new DragEvent(event), tl.onDetect(Seq(tl.value.size, index.value).min))
    event.consume()
  }
  private def remove(): Unit = {
    if(empty.value) return
    tl.value.remove(index.value)
    tl.valueLogger.logging(tl.value.toVector)
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
                          .reverse.foreach(index=> tl.value.remove(index))
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
      val targetIndex = Seq(index.value, tl.value.size).min
      tl.value.addAll(targetIndex, sources.asJava)
      event.setDropCompleted(true)
      /* select */ val selectionModel = listView.get().getSelectionModel
        selectionModel.clearSelection()
        selectionModel.selectRange(targetIndex, targetIndex + sources.size)
      tl.valueLogger.logging(tl.value.toVector)
    }
  }
}
