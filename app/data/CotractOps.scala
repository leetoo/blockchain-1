package data

import java.util.UUID

import models.{Contract, NewContract}

import scala.concurrent.Future

trait ContractOpsComponent {
  def contractOps: ContractOps

  trait ContractOps {
    def create(newContract: NewContract): Future[Contract]
    def getAllForCustomer(uuid: UUID): Future[Seq[Contract]]
    def getAllForExecutor(uuid: UUID): Future[Seq[Contract]]
    def update(contract: Contract): Future[Option[Contract]]
    def getAll(): Future[Seq[Contract]]
  }
}

trait DefaultContractOpsComponent extends ContractOpsComponent {
  this: ContractDataComponent =>

  import play.api.libs.concurrent.Execution.Implicits._

  class DefaultContractOps extends ContractOps {
    override def create(newContract: NewContract): Future[Contract] =
      contractData.create(newContract)

    override def getAllForCustomer(uuid: UUID): Future[Seq[Contract]] =
      contractData.getAllForCustomer(uuid)

    override def getAllForExecutor(uuid: UUID): Future[Seq[Contract]] =
      contractData.getAllForExecutor(uuid)

    override def update(contract: Contract): Future[Option[Contract]] =
      contractData.update(contract)

    override def getAll(): Future[Seq[Contract]] =
      contractData.getAll()
  }
}

trait ContractDataComponent {
  def contractData: ContractData

  trait ContractData {
    def create(newContract: NewContract): Future[Contract]
    def getAllForCustomer(uuid: UUID): Future[Seq[Contract]]
    def getAllForExecutor(uuid: UUID): Future[Seq[Contract]]
    def update(contract: Contract): Future[Option[Contract]]
    def getAll(): Future[Seq[Contract]]
  }
}

trait DefaultContractDataComponent extends ContractDataComponent {
  import play.api.libs.concurrent.Execution.Implicits._

  class DefaultContractData extends ContractData {
    private[this] val contracts = collection.mutable.ArrayBuffer.empty[Contract]

    override def getAll() = Future {
      contracts.toList
    }

    override def create(newContract: NewContract): Future[Contract] = Future {
      val contract = Contract(UUID.randomUUID(), newContract.name, newContract.sustomerUUID,
        newContract.customerName, Seq(), "", newContract.price)
      contracts += contract
      contract
    }

    override def update(contract: Contract): Future[Option[Contract]] = Future {
      val contractOpt = contracts.find(_.uuid == contract.uuid)
      contractOpt.foreach(c => {
        contracts -= c
        contracts += contract
      })
      contractOpt.map(_ => contract)
    }

    override def getAllForCustomer(uuid: UUID): Future[Seq[Contract]] = Future {
      contracts.filter(_.uuid == uuid)
    }

    override def getAllForExecutor(uuid: UUID): Future[Seq[Contract]] = Future {
      contracts.filter(_.executors.exists(_.uuid == uuid))
    }
  }
}