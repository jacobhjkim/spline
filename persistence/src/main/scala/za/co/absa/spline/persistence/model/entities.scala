/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package za.co.absa.spline.persistence.model

trait ArangoDocument {
  // entity creation time (don't confuse with the event time)
  val _created: ArangoDocument.Timestamp = System.currentTimeMillis

  // Arango ID of the parent/aggregate entity, if this document is an aggregate component.
  // It is useful for cases beyond the graph model when edges cannot be used for the same purpose.
  // E.g. to bind a sub-graph (including edges) or a set of arbitrary documents (that aren't nodes)
  // into a logical aggregate represented by another document.
  val _belongsTo: Option[ArangoDocument.Id]
}

object ArangoDocument {
  type Id = String
  type Key = String
  type Timestamp = Long
}

trait RootEntity {
  this: ArangoDocument =>
  override val _belongsTo: Option[ArangoDocument.Id] = None
}

trait Vertex extends ArangoDocument {
  def _key: ArangoDocument.Key
}

case class Edge(
  _from: ArangoDocument.Id,
  _to: ArangoDocument.Id,
  override val _belongsTo: Option[ArangoDocument.Id],
  index: Option[Edge.Index],
  path: Option[Edge.FromPath]
) extends ArangoDocument {
  def this() = this(null, null, null, null, null)
}

object Edge {
  type Index = Int // 0-based number reflecting the position among sibling edges of the same type sharing the same {{_from}}
  type FromPath = String // JSONPath (by S. Gössner) of the exact property in {{_from}} that points the {{_to}}
}

case class DBVersion(
  version: String,
  status: String
) extends ArangoDocument with RootEntity {
  def this() = this(null, null)
}

object DBVersion {
  def apply(version: String, status: Status.Type): DBVersion = DBVersion(version, status.toString)

  object Status extends Enumeration {

    type Type = Value

    val Current: Status.Type = Value("current")
    val Preparing: Status.Type = Value("preparing")
    val Upgraded: Status.Type = Value("upgraded")
  }

}

/**
  * Represents a named location WHERE data can be read from or written to.
  * It can be anything that can serve as a data input or output for a data pipeline.
  * E.g. file or directory on a filesystem, table in the database, topic in Kafka etc.
  */
case class DataSource(
  uri: DataSource.Uri,
  override val _key: DataSource.Key
) extends Vertex with RootEntity {
  def this() = this(null, null)
}

object DataSource {
  type Key = ArangoDocument.Key
  type Uri = String
}

/**
  * Represents an execution plan.
  * Contains all static information about HOW data is transformed along the way
  * from the inputs to the output.
  */
case class ExecutionPlan(
  systemInfo: Map[String, Any],
  agentInfo: Map[String, Any],
  extra: Map[String, Any],
  override val _key: ArangoDocument.Key
) extends Vertex with RootEntity

/**
  * Represents a moment in time WHEN a particular execution plan is executed.
  * It can also hold the result of the execution and related stats, and any other
  * custom data logically connected to the event.
  */
case class Progress(
  timestamp: Long,
  error: Option[Any],
  extra: Map[String, Any],
  override val _key: String,
  execPlanDetails: ExecPlanDetails
) extends Vertex with RootEntity

/**
  * These values are copied from other entities for performance optimization.
  */
case class ExecPlanDetails(
  executionPlanId: ArangoDocument.Key,
  frameworkName: String,
  applicationName: String,
  dataSourceUri: DataSource.Uri,
  dataSourceType: String,
  append: Boolean
)
