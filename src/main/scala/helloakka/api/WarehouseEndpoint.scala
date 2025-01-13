package helloakka.api

import akka.javasdk.annotations.Acl
import akka.javasdk.annotations.http.{Get, HttpEndpoint}

import java.util.concurrent.CompletionStage
import java.util.concurrent.CompletableFuture.completedStage

@Acl(allow = Array(new Acl.Matcher(principal = Acl.Principal.INTERNET)))
@HttpEndpoint("/warehouse")
class WarehouseEndpoint:

  @Get("/")
  def search(): CompletionStage[String] = completedStage("Search Warehouse")
