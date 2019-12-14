package com.github.nnnnusui.slideshow

class StateLogger[T](initValue: T){
  private var timelineLog: List[T] = List(initValue)
  private var logAccessIndex = 0
  def logging(value: T): Unit ={
    val log = timelineLog.drop(logAccessIndex)
    timelineLog = value :: log.take(9)
    logAccessIndex = 0
  }
  def undo(): T ={
    if(logAccessIndex +1 < timelineLog.size)
      logAccessIndex += 1
    timelineLog(logAccessIndex)
  }
  def redo(): T = {
    if(logAccessIndex > 0)
      logAccessIndex -= 1
    timelineLog(logAccessIndex)
  }
}
