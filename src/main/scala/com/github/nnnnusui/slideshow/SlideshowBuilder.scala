package com.github.nnnnusui
package slideshow

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{FileChooser, Stage}

object SlideshowBuilder extends JFXApp{
  val timeline = new Timeline
  private val saveButton = new Button("to .exo"){ onAction = _=> saveToExo() }
  private val settingsButton = new Button("settings"){ onAction = _=> dialog.showAndWait() }
  private val label = Label("")
  timeline.value.onChange{ (_, _)=> label.text = timeline.value.size.toString }

  stage = new PrimaryStage{
    title = "Slideshow Builder"
    scene = new Scene{
      root = new BorderPane{
        center = timeline.view
        bottom = new BorderPane{
          left = settingsButton
          center = label
          right = saveButton
        }
      }
    }
  }
  object Parameter{
    var width = 1280
    var height = 720
    var fps = 30
    var bpm = 120
    var step = 4
  }
  val dialog: Stage = new Stage{
    scene = new Scene{
      root = new BorderPane{
        private val widthField  = new TextField() { text = Parameter.width.toString }
        private val heightField = new TextField() { text = Parameter.height.toString }
        private val fpsField    = new TextField() { text = Parameter.fps.toString }
        private val bpmField    = new TextField() { text = Parameter.bpm.toString }
        private val stepField   = new TextField() { text = Parameter.step.toString }
        center = new GridPane{
          Seq((Label("width")  , widthField)
            , (Label("height") , heightField)
            , (Label("fps")    , fpsField)
            , (Label("bpm")    , bpmField)
            , (Label("step")   , stepField)
          ).zipWithIndex.foreach{case((label, field), index)=> addRow(index, label, field)}
        }
        bottom = new BorderPane{
          right = new Button("apply"){
            onAction = _=>{
              Parameter.width  = widthField.text.value.toInt
              Parameter.height = heightField.text.value.toInt
              Parameter.fps    = fpsField.text.value.toInt
              Parameter.bpm    = bpmField.text.value.toInt
              Parameter.step   = stepField.text.value.toInt
              close()
            }
          }
          left = new Button("cancel"){ onAction = _=> close() }
        }
      }
    }
  }

  def saveToExo(): Unit ={
    val chooser = new FileChooser()
    chooser.initialDirectory = Paths.get(".").toFile
    chooser.extensionFilters.add(new ExtensionFilter("AviUtl", "*.exo"))
    chooser.initialFileName = "template"
    val result = chooser.showSaveDialog( stage )
    if (result == null) return
    if (timeline.value.isEmpty) return

    import Parameter._
    val header = Exo.Header(width, height, fps)
    val timelineObjects = Timeline.Object.toExoObjects(timeline.value.toList, fps, bpm, step)
    val exo = Exo(header, timelineObjects)
    Files.writeString(result.toPath, exo.toExo, Charset.forName("Shift_JIS"))
  }
}
