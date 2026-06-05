// ADS I Class Project
// Assignment 02: ALU UVM Sequence Item

`ifndef ALU_SEQ_ITEM_SV
`define ALU_SEQ_ITEM_SV

class alu_seq_item extends uvm_sequence_item;

  rand bit [31:0] operandA;
  rand bit [31:0] operandB;
  rand ALUOp operation;

  bit [31:0] aluResult;

  `uvm_object_utils_begin(alu_seq_item)
    `uvm_field_int(operandA, UVM_DEFAULT)
    `uvm_field_int(operandB, UVM_DEFAULT)
    `uvm_field_enum(ALUOp, operation, UVM_DEFAULT)
    `uvm_field_int(aluResult, UVM_DEFAULT)
  `uvm_object_utils_end

  constraint aluOp_constraint {
    operation inside {
      ADD,
      SUB,
      AND,
      OR,
      XOR,
      SLL,
      SRL,
      SRA,
      SLT,
      SLTU,
      PASSB
    };
  }

  function new(string name = "alu_seq_item");
    super.new(name);
  endfunction

  virtual function string convert2str();
    return $sformatf(
      "operandA: 0x%08h, operandB: 0x%08h, operation: %s, aluResult: 0x%08h",
      operandA,
      operandB,
      operation.name(),
      aluResult
    );
  endfunction

endclass

`endif