package com.mattmerr.hacc;

import static com.mattmerr.hacc.HTypeString.visitType;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAdd;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCall;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCondBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildExtractValue;
import static org.bytedeco.javacpp.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildICmp;
import static org.bytedeco.javacpp.LLVM.LLVMBuildInsertValue;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRet;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMBuildSub;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMCreateBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMGetElementType;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMGetStructName;
import static org.bytedeco.javacpp.LLVM.LLVMGetTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMIntNE;
import static org.bytedeco.javacpp.LLVM.LLVMModuleCreateWithName;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;

import com.mattmerr.hihi.HProg;
import com.mattmerr.hitch.parsetokens.ParseNodeAssignedDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeCall;
import com.mattmerr.hitch.parsetokens.ParseNodeDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.ParseNodeExpressionStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeFunctionDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeIfStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeReturnStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeWhileStatement;
import com.mattmerr.hitch.parsetokens.expression.BinaryOperation;
import com.mattmerr.hitch.parsetokens.expression.Call;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.parsetokens.expression.Literal;
import com.mattmerr.hitch.parsetokens.expression.Operation;
import com.mattmerr.hitch.parsetokens.expression.Operation.OperationType;
import com.mattmerr.hitch.parsetokens.expression.Variable;
import com.mattmerr.hitch.tokens.TokenType;
import com.mattmerr.hitch.tokens.Value;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class Ast2LLVM {

  public static LLVMModuleRef create(HProg prog) {
    LLVMModuleRef mod = LLVMModuleCreateWithName("my_module");

    LLVMTypeRef paramTypes[] = {LLVMInt32Type(), LLVMInt32Type()};
    LLVMTypeRef retType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(paramTypes), 2, 0);
    LLVMValueRef sum = LLVMAddFunction(mod, "sum", retType);
    LLVMBasicBlockRef entry = LLVMAppendBasicBlock(sum, "entry");
    LLVMBuilderRef builder = LLVMCreateBuilder();
    LLVMPositionBuilderAtEnd(builder, entry);

    LLVMValueRef tmp = LLVMBuildAdd(builder, LLVMGetParam(sum, 0), LLVMGetParam(sum, 1), "tmp");
    LLVMBuildRet(builder, tmp);

    return mod;
  }

  public static HScope visit(CompiledFile file) {
//    LLVMModuleRef mod = LLVMModuleCreateWithName("my_module");
    LLVMBuilderRef builder = LLVMCreateBuilder();

    HScope scope = file.scope;//new HScope(mod);//file.scope;
    scope.declareStandardFunctions();

    scope.addImplementers(HNativeFunctions.NATIVE_FUNCTION_IMPLEMENTERS);

//    scope.addImplementer(VTableBuilder::generateVTable);
//    scope.addImplementer(VTableBuilder::sinkVTable);
    scope.addImplementer((s) -> visit(s, builder, null, file.parseTree.block));

//    scope.addImplementers(HNativeStringFunctions.STRING_FUNCTION_IMPLEMENTERS);

    scope.implement();
    return scope;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeStatement statement) {
    if (statement instanceof ParseNodeBlock) {
      return visit(scope, builderRef, blockRef, (ParseNodeBlock) statement);
    }
    else if (statement instanceof ParseNodeFunctionDeclaration) {
      return visit(scope, builderRef, blockRef, (ParseNodeFunctionDeclaration) statement);
    }
    else if (statement instanceof ParseNodeCall) {
      return visit(scope, builderRef, blockRef, (ParseNodeCall) statement);
    }
    else if (statement instanceof ParseNodeIfStatement) {
      return visit(scope, builderRef, blockRef, (ParseNodeIfStatement) statement);
    }
    else if (statement instanceof ParseNodeDeclaration) {
      return visit(scope, builderRef, blockRef, (ParseNodeDeclaration) statement);
    }
    else if (statement instanceof ParseNodeWhileStatement) {
      return visit(scope, builderRef, blockRef, (ParseNodeWhileStatement) statement);
    }
    else if (statement instanceof ParseNodeExpressionStatement) {
      return visit(scope, builderRef, ((ParseNodeExpressionStatement) statement).expression.root);
    }
    throw new UnsupportedOperationException("No known visit for " + statement.getClass());
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeFunctionDeclaration decl) {

    ParseNodeFunction function = decl.function;
    LLVMValueRef func = scope.get(function.name);
    HScope innerScope = scope.childScope();

    for (int paramIdx = 0; paramIdx < function.argumentTypes.size(); paramIdx++) {
      String paramName = function.argumentMapping.get(paramIdx);
      LLVMTypeRef type = visitType(scope, function.argumentTypes.get(paramIdx));
      innerScope.declare(paramName, LLVMPointerType(type, 0));
      LLVMValueRef paramPtr = LLVMBuildAlloca(builderRef, type, "");

//      LLVMBuildStore(builderRef, paramPtr, LLVMGetParam(func, paramIdx));//, paramPtr);
      innerScope.put(paramName, LLVMGetParam(func, paramIdx));
    }

    LLVMBasicBlockRef entry = LLVMAppendBasicBlock(func, "");
    LLVMBuilderRef builder = LLVMCreateBuilder();

    LLVMPositionBuilderAtEnd(builder, entry);
    visit(innerScope, builder, entry, function.definition);

//    LLVMValueRef res = LLVMBuildPhi(builder, LLVMInt32Type(), "result");
    if ("void".equals(decl.function.returnType)) {
      LLVMBuildRet(builder, null);
    }

    // TODO(matthewmerrill): Return val
//    LLVMValueRef val = visit(mod, function.definition);
//    LLVMBuildRet(builder, val);

    return func;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeIfStatement ifStatement) {

    if (ifStatement.ifFalseStatement == null) {
      LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
      LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "end");

      LLVMValueRef conditionVal = visit(scope, builderRef, ifStatement.conditionExpression.root);

      LLVMValueRef ifRef = visitTruthy(scope, builderRef, conditionVal);
      LLVMBuildCondBr(builderRef, ifRef, ifTrue, end);

      LLVMPositionBuilderAtEnd(builderRef, ifTrue);
      visit(scope, builderRef, ifTrue, ifStatement.ifTrueStatement);
      LLVMBuildBr(builderRef, end);

      LLVMPositionBuilderAtEnd(builderRef, end);
      return ifRef;
    }
    else {
      LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
      LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
      LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");

      LLVMValueRef conditionVal = visit(scope, builderRef, ifStatement.conditionExpression.root);
      LLVMValueRef ifRef = visitTruthy(scope, builderRef, conditionVal);
      LLVMBuildCondBr(builderRef, ifRef, ifTrue, ifFalse);

      LLVMPositionBuilderAtEnd(builderRef, ifTrue);
      visit(scope, builderRef, ifTrue, ifStatement.ifTrueStatement);
      LLVMBuildBr(builderRef, end);

      LLVMPositionBuilderAtEnd(builderRef, ifFalse);
      visit(scope, builderRef, ifFalse, ifStatement.ifFalseStatement);
      LLVMBuildBr(builderRef, end);

      LLVMPositionBuilderAtEnd(builderRef, end);
      return ifRef;
    }
  }

  private static LLVMValueRef visitTruthy(HScope scope, LLVMBuilderRef builderRef,
      LLVMValueRef conditionVal) {
    if ("int".equals(LLVMGetStructName(LLVMTypeOf(conditionVal)).getString())) {
      return LLVMBuildICmp(builderRef, LLVMIntNE,
          LLVMBuildExtractValue(builderRef, conditionVal,
              HTypeString.getMemberIndex(LLVMTypeOf(conditionVal), "value"), ""),
          LLVMConstInt(LLVMInt32Type(), 0, 0), "");
    }
    throw new RuntimeException("Not all values supported for truthiness.");
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeWhileStatement whileStatement) {

    LLVMBasicBlockRef loopBody = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
    LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "end");

    LLVMBuildCondBr(builderRef, visitTruthy(scope, builderRef,
        visit(scope, builderRef, whileStatement.conditionExpression.root)), loopBody, end);

    LLVMPositionBuilderAtEnd(builderRef, loopBody);
    visit(scope, builderRef, loopBody, whileStatement.statement);
    LLVMBuildCondBr(builderRef, visitTruthy(scope, builderRef,
        visit(scope, builderRef, whileStatement.conditionExpression.root)), loopBody, end);

    LLVMPositionBuilderAtEnd(builderRef, end);
    return null;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeDeclaration declaration) {

    LLVMTypeRef type = visitType(scope, declaration.type);
    LLVMValueRef valueRef = LLVMBuildAlloca(builderRef, type, "");
    scope.declare(declaration.qualifiedIdentifier, type);
    scope.put(declaration.qualifiedIdentifier, valueRef);

    if (declaration instanceof ParseNodeAssignedDeclaration) {
      ParseNodeAssignedDeclaration assignment = (ParseNodeAssignedDeclaration) declaration;
      LLVMBuildStore(builderRef, visit(scope, builderRef, assignment.assignmentExpression.root),
          valueRef);
    }

    return valueRef;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeBlock block) {
    for (ParseNodeStatement statement : block.getStatementList()) {
      visit(scope, builderRef, blockRef, statement);
    }
    return null;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeCall call) {
    LLVMValueRef callArgs[] = new LLVMValueRef[call.arguments.size()];

    for (int i = 0; i < callArgs.length; i++) {
      callArgs[i] = visit(scope, builderRef, call.arguments.get(i));
    }

    return LLVMBuildCall(builderRef, scope.get(call.qualifiedFunction),
        new PointerPointer<>(callArgs), callArgs.length, "");
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeReturnStatement returnStatement) {
    return LLVMBuildRet(builderRef, visit(scope, builderRef, returnStatement.value));
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      ParseNodeExpression expression) {
    LLVMValueRef valueRef = visit(scope, builderRef, expression.root);
    LLVMValueRef valuePtr = LLVMBuildAlloca(builderRef, LLVMTypeOf(valueRef), "");
    LLVMBuildStore(builderRef, valueRef, valuePtr);
    return valuePtr;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      ExpressionToken expressionToken) {
    return visit(scope, builderRef, expressionToken, false);
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      ExpressionToken expressionToken, boolean leaveAsPointer) {
    if (expressionToken instanceof Literal) {
      Literal literal = (Literal) expressionToken;
      Value value = literal.value;

      if (value.type == TokenType.STRING) {
        LLVMTypeRef typeRef = visitType(scope, "string");
        LLVMValueRef valuePtr = LLVMBuildAlloca(builderRef, typeRef, "");
        LLVMValueRef valueRef = LLVMBuildInsertValue(builderRef,
            LLVMBuildLoad(builderRef, valuePtr, ""),
            LLVMBuildGlobalStringPtr(builderRef, (String) value.value, ""), 1, "");
        LLVMBuildStore(builderRef, valueRef, valuePtr);
        return valueRef;
      }
      else if (value.type == TokenType.INTEGER) {
        LLVMTypeRef typeRef = visitType(scope, "int");
        LLVMValueRef valuePtr = LLVMBuildAlloca(builderRef, typeRef, "");
        LLVMValueRef valueRef = LLVMBuildInsertValue(builderRef,
            LLVMBuildLoad(builderRef, valuePtr, ""),
            LLVMConstInt(LLVMInt32Type(), (Integer) value.value, 0), 1, "");
        LLVMBuildStore(builderRef, valueRef, valuePtr);
        return valueRef;
      }
    }
    else if (expressionToken instanceof Variable) {
      Variable var = (Variable) expressionToken;
      LLVMValueRef valueRef = scope.get(var.qualifiedName);

      if (valueRef == null) {
        throw new RuntimeException("Undeclared variable: " + var.qualifiedName);
      }

      if (leaveAsPointer
          || LLVMGetTypeKind(LLVMGetElementType(LLVMTypeOf(valueRef))) == LLVMFunctionTypeKind) {
        return valueRef;
      }

      return LLVMBuildLoad(builderRef, valueRef, "");
    }
    else if (expressionToken instanceof Call) {
      Call call = (Call) expressionToken;
      LLVMValueRef callArgs[] = new LLVMValueRef[call.arguments.size()];

      for (int i = 0; i < callArgs.length; i++) {
        callArgs[i] = visit(scope, builderRef, call.arguments.get(i));
      }

      return LLVMBuildCall(builderRef, visit(scope, builderRef, call.variable),
          new PointerPointer<>(callArgs), callArgs.length, "");
    }
    else if (expressionToken instanceof Operation) {
      Operation operation = (Operation) expressionToken;

      if (operation.type == OperationType.ADD) {
        BinaryOperation binop = (BinaryOperation) operation;
        LLVMValueRef leftRef = visit(scope, builderRef, binop.left);
        LLVMValueRef rightRef = visit(scope, builderRef, binop.right);

        return LLVMBuildAdd(builderRef, leftRef, rightRef, "");
      }
      else if (operation.type == OperationType.SUBTRACT) {
        BinaryOperation binop = (BinaryOperation) operation;
        LLVMValueRef leftRef = visit(scope, builderRef, binop.left);
        LLVMValueRef rightRef = visit(scope, builderRef, binop.right);

        return LLVMBuildSub(builderRef, leftRef, rightRef, "");
      }
      else if (operation.type == OperationType.ASSIGN) {
        BinaryOperation binop = (BinaryOperation) operation;
        LLVMValueRef leftRef = visit(scope, builderRef, binop.left, true);
        LLVMValueRef rightRef = visit(scope, builderRef, binop.right);

        return LLVMBuildStore(builderRef, rightRef, leftRef);
      }
      else if (operation.type == OperationType.ACCESS) {
        BinaryOperation binop = (BinaryOperation) operation;

        if (binop.left instanceof Variable && scope
            .isDependency(((Variable) binop.left).qualifiedName)) {
          if (binop.right instanceof Variable) {
            return scope.get(
                scope.getDependency((((Variable) binop.left).qualifiedName)).id +
                    "." + ((Variable) binop.right).qualifiedName);
//            return scope.getDependency(((Variable) binop.left).qualifiedName).scope
//                .get(((Variable) binop.right).qualifiedName);
          }
        }
        throw new RuntimeException(
            "Only accesses operations currently supported are resolving a dependency's function");
      }
    }
//        if ("String".equals(getType(scope, binop.left).name) && "String".equals(getType(scope,
// binop.right).getType().name)) {
//          LLVMValueRef leftRef = visit(scope, builderRef, binop.left);
//          LLVMValueRef rightRef = visit(scope, builderRef, binop.left);
//          return LLVMBuildCall(builderRef, scope.get("llvm.memcpy.p0i8.p0i8.i32"), new
// PointerPointer<>(
//              leftRef,
//              rightRef,
//              LLVMConstInt(LLVMInt32Type(), 1, 0),
//              LLVMConstInt(LLVMInt32Type(), 1, 0),
//              LLVMConstInt(LLVMInt1Type(), 1, 0)
//          ), 5, "");
//        }
//      }
//    }

    throw new UnsupportedOperationException("Not all expressions supported.");
  }


}
