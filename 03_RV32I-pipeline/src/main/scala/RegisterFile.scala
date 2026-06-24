
/*
Register File Module: 32x32-bit dual-read single-write register file

Memory:
    regFile: Register file according to the RISC-V 32I specification

Ports:
    req_1, resp_1: first read port
        req_1.addr: read address for register x[0-31]
        resp_1.data: register data output
    req_2, resp_2: second read port
        req_2.addr: read address for register x[0-31]
        resp_2.data: register data output
    req_3: write port
        req_3.addr: write destination address
        req_3.data: data to write
        req_3.wr_en: write enable signal

Functionality:
    Two read ports allow simultaneous reading of two operands
    Synchronous write updates register if wr_en is asserted
*/

// -----------------------------------------
// Register File
// -----------------------------------------


package core_tile

import chisel3._

// Read request contains register address
class regFileReadReq extends Bundle {
  val addr = UInt(5.W)
}

// Read response contains register data
class regFileReadResp extends Bundle {
  val data = UInt(32.W)
}

// Write request contains address, data, and write enable
class regFileWriteReq extends Bundle {
  val addr  = UInt(5.W)
  val data  = UInt(32.W)
  val wr_en = Bool()
}

class regFile extends Module {
  val io = IO(new Bundle {
    // First read port for rs1
    val req_1  = Input(new regFileReadReq)
    val resp_1 = Output(new regFileReadResp)

    // Second read port for rs2
    val req_2  = Input(new regFileReadReq)
    val resp_2 = Output(new regFileReadResp)

    // One write port for rd
    val req_3 = Input(new regFileWriteReq)
  })

  // 32 registers, each 32-bit wide
  val registers = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // Read port 1
  // x0 must always return 0
  io.resp_1.data := Mux(io.req_1.addr === 0.U, 0.U, registers(io.req_1.addr))

  // Read port 2
  // x0 must always return 0
  io.resp_2.data := Mux(io.req_2.addr === 0.U, 0.U, registers(io.req_2.addr))

  // Write data only when write enable is true
  // Do not allow writing to x0
  when(io.req_3.wr_en && io.req_3.addr =/= 0.U) {
    registers(io.req_3.addr) := io.req_3.data
  }

  // Force x0 to remain zero
  registers(0) := 0.U
}