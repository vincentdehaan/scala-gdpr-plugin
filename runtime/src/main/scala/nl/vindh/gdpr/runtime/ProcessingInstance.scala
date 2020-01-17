package nl.vindh.gdpr.runtime

import scala.annotation.Annotation

class ProcessingInstance[T <: ProcessingInstanceRecord](record: T) extends Annotation {

}