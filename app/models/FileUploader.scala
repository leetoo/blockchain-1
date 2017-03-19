package models

import scala.concurrent.Future

trait FileUploaderComponent {
  def fileUploader: FileUploader

  trait FileUploader {
    def upload(file: File): Future[Unit]
  }
}

trait DefaultFileUploaderComponent extends FileUploaderComponent {

//  class DefaultFileUploader extends FileUploader {
//    override def upload()
//  }
}
