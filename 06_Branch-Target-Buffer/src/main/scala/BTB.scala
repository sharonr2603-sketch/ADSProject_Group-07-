// ADS I Class Project
// Pipelined RISC-V Core - two-way set-associative Branch Target Buffer
package core_tile

import chisel3._
import chisel3.util._

class BTB extends Module {
  private val sets = 8 // Number of sets in the BTB 
  private val ways = 2 //// Number of ways per set(2 way set associative BTB)
  private val weakNotTaken = 1.U(2.W) // Initial predictor state--to avoid strong initial bias

  val io = IO(new Bundle {
    val PC          = Input(UInt(32.W))  // Current Program Counter used for BTB lookup
    val update      = Input(Bool())  // Enables updating the BTB after branch execution
    val updatePC    = Input(UInt(32.W))  //updating pc of the branch
    val updateTarget = Input(UInt(32.W)) //actual branch tgt address to store
    val mispredicted = Input(Bool()) // indicates wether branch direction was mispredicted

    val valid       = Output(Bool())  //indicates wether valid btb entry was found
    val target      = Output(UInt(32.W)) //predicted branch tgt address
    val predictTaken = Output(Bool()) //predicted branch direction
  })

  // PC[4:2] selects one of eight word-addressed sets; PC[31:5] is the tag.
  val validBits = RegInit(VecInit(Seq.fill(sets)(VecInit(Seq.fill(ways)(false.B))))) // Stores the valid bit for every BTB entry.
  val tags      = RegInit(VecInit(Seq.fill(sets)(VecInit(Seq.fill(ways)(0.U(27.W)))))) //storees the tags for every btb entry
  val targets   = RegInit(VecInit(Seq.fill(sets)(VecInit(Seq.fill(ways)(0.U(32.W)))))) //stores the predicted branch tgt address
  val counters  = RegInit(VecInit(Seq.fill(sets)(VecInit(Seq.fill(ways)(weakNotTaken)))))  //stores the 2bit branch prediction counter
  // lru(set) is the way to replace when both ways are valid.
  val lru       = RegInit(VecInit(Seq.fill(sets)(0.U(1.W))))  //// Stores the Least Recently Used (LRU) way for each set (0 or 1)

  val lookupSet = io.PC(4, 2) //// Extract the set index from the Program Counter
  val lookupTag = io.PC(31, 5) //extract tag from pc (except and ,00)
  val hit0 = validBits(lookupSet)(0) && tags(lookupSet)(0) === lookupTag //check for hit in way 0 and below line checks for 1
  val hit1 = validBits(lookupSet)(1) && tags(lookupSet)(1) === lookupTag

  io.valid := hit0 || hit1  // Output whether either way contains a matching BTB entry//T-F,F-T,F-F is btb miss
  io.target := Mux(hit0, targets(lookupSet)(0), targets(lookupSet)(1))  // Output the predicted target address{way0hit->way0 tgt, way1hit->way1 tgt}
  io.predictTaken := Mux(hit0, counters(lookupSet)(0)(1), counters(lookupSet)(1)(1))  //o/p predicted branch direction

  // A lookup makes the opposite way least recently used.
  when(hit0) {  //update the lru info when btb hit
    lru(lookupSet) := 1.U  //marking 1 as least recently used
  }.elsewhen(hit1) {
    lru(lookupSet) := 0.U
  }

  val updateSet = io.updatePC(4, 2)  // Extract the set index for the BTB update
  val updateTag = io.updatePC(31, 5) // Extract the tag for the BTB update
  val updateHit0 = validBits(updateSet)(0) && tags(updateSet)(0) === updateTag
  val updateHit1 = validBits(updateSet)(1) && tags(updateSet)(1) === updateTag // Check whether the branch already exists in way 1
  val updateWay = Wire(UInt(1.W)) //select the btb way to update
  updateWay := 0.U //default to updating way 0
  when(updateHit1) {  //update way 1 if entry already exists there //Because the branch already exists in the BTB. We only update the existing entry
    updateWay := 1.U
  }.elsewhen(!updateHit0 && validBits(updateSet)(0)) {  // Select an empty way or replace the LRU entry
    when(!validBits(updateSet)(1)) {
      updateWay := 1.U
    }.otherwise {
      updateWay := lru(updateSet)
    }
  }

  // The old prediction plus its correctness reconstructs the actual outcome
  val oldCounter = Wire(UInt(2.W))  // Read the current prediction counter
  oldCounter := weakNotTaken  // Default predictor state
  when(updateHit0) {  // Read the counter from way 0
    oldCounter := counters(updateSet)(0)
  }.elsewhen(updateHit1) {  // Read the counter from way 1
    oldCounter := counters(updateSet)(1)
  }
  val actualTaken = oldCounter(1) ^ io.mispredicted // Recover the actual branch outcome...ex sends mispredicted
  val nextCounter = Wire(UInt(2.W)) // Compute the updated predictor state
  nextCounter := oldCounter // Initialize with the current stat
  when(actualTaken && oldCounter =/= 3.U) {  // Move toward Strong Taken after a taken branch
    nextCounter := oldCounter + 1.U
  }.elsewhen(!actualTaken && oldCounter =/= 0.U) {  // Move toward Strong Not Taken after a not-taken branch
    nextCounter := oldCounter - 1.U
  }

  when(io.update) {  // Update the BTB entry after branch execution...only update when ex stage tells us
    validBits(updateSet)(updateWay) := true.B //mark the btb entry as valid...because btb entry has been created and can be used for future predictions
    tags(updateSet)(updateWay) := updateTag //// Store the branch tag
    targets(updateSet)(updateWay) := io.updateTarget //// Store the branch target address
    counters(updateSet)(updateWay) := nextCounter // Store the updated predictor state
    lru(updateSet) := ~updateWay // Update the LRU information after replacement
  }
}
