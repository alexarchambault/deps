/**
 * Provides values from `deps.json` ready to be consumed from sbt.
 *
 * Dependencies, like
 * {{{
 * {
 *   "dependencies": {
 *     "org:name": "ver"
 *   }
 * }
 * }}}
 * can be accessed via either of
 * {{{
 * Deps.`org:name`
 * Deps.`name`
 * // both equal "org" % "name" % "ver"
 * }}}
 * If {Deps.`name`} is ambiguous, use {Deps.`org:name`}.
 *
 * Versions of dependencies can be directly accessed with
 * {{{
 * Deps.V.`org:name`
 * Deps.V.`name`
 * // both equal "ver"
 * }}}
 *
 * Top-level versions, like in
 * {{{
 * {
 *   "scala": ["2.12.10", "2.13.1"],
 *   "foo": "1.2.3"
 * }
 * }}}
 * can be accessed with
 * {{{
 * Deps.scalaVersions // Seq("2.12.10", "2.13.1")
 * Deps.scalaVersion // "2.13.1" // last one from the array
 * Deps.fooVersions // Seq("1.2.3")
 * Deps.fooVersion // "1.2.3"
 * }}}
 */
object Deps extends deps_.Deps
