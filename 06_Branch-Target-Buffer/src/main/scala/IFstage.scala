//The IF stage is responsible for fetching the instruction from instruction memory and updating the Program Counter (PC) for the next instruction.

package core_tile

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile

class IF(BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val redirect       = Input(Bool())  //says that The prediction was wrong, and the processor flushes wrong path and jumps to correct address ie  Indicates whether the PC should be redirected.
    val redirectPC     = Input(UInt(32.W)) //correct PC after Misprediction

    val predictTaken   = Input(Bool())  //if true , if stage jumps immediately 
    val predictedTarget = Input(UInt(32.W))  //address predicted by btb

    val instr          = Output(UInt(32.W))  //outputs the fetched instruction
    val pc             = Output(UInt(32.W))  //outputs the current pc

    val outPredictTaken = Output(Bool()) //passes the predicted info to next stage 
    val outPredictedTarget = Output(UInt(32.W))  //also forwarded to later stages
  })

  val PC = RegInit(0.U(32.W))  // Read the instruction at the current Program Counter (PC).
  val IMem = Mem(4096, UInt(32.W))  //creates instruction mem
  loadMemoryFromFile(IMem, BinaryFile) //loads binaryfile to imem , now program can fetch instructions

  io.instr := IMem(PC(13, 2))
  io.pc := PC  //sned current pc to next stage
  io.outPredictTaken := io.predictTaken //pass to next stage
  io.outPredictedTarget := io.predictedTarget //pass

//// Update PC: redirect to the correct PC on misprediction; otherwise jump to the BTB-predicted target if the branch is predicted taken.
  when(io.redirect) {
    PC := io.redirectPC
  }.elsewhen(io.predictTaken) {
    PC := io.predictedTarget
  }.otherwise {
    PC := PC + 4.U //if: no redirect or no prediction , execute normally
  }
}
