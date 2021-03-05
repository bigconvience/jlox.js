package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.JSAtom.JS_ATOM_TYPE_STRING;
import static com.craftinginterpreters.lox.JSClassID.JS_CLASS_OBJECT;
import static com.craftinginterpreters.lox.JSValue.JS_EXCEPTION;
import static com.craftinginterpreters.lox.OPCodeEnum.OP_invalid;
import static com.craftinginterpreters.lox.OPCodeEnum.OP_COUNT;

/**
 * @author benpeng.jiang
 * @title: JSContext
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/222:00 PM
 */
public class JSContext {
  static final int DEFINE_GLOBAL_LEX_VAR = (1 << 7);
  static final int DEFINE_GLOBAL_FUNC_VAR = (1 << 6);

  static final int JS_CALL_FLAG_COPY_ARGV = (1 << 1);
  static final int JS_CALL_FLAG_GENERATOR = (1 << 2);
  static final int JS_MAX_LOCAL_VARS = 65536;
  static final int JS_STACK_SIZE_MAX = 65536;
  static final int JS_STRING_LEN_MAX = ((1 << 30) - 1);

  final Map<JSClassID, JSValue> class_proto;

  public JSValue globalObj;
  final JSRuntime rt;

  public JSContext(JSRuntime rt) {
    this.rt = rt;
    class_proto = new HashMap<>();
  }

  JSValue JS_AtomToValue(int atomIdx) {
    JSAtom atom = new JSAtom(atomIdx);
    return JS_AtomToValue(atom);
  }

  JSValue JS_AtomToValue(JSAtom atom) {
    return __JS_AtomToValue(atom, false);
  }

  JSValue JS_AtomToString(JSAtom atom) {
    return __JS_AtomToValue(atom, true);
  }

  private JSValue __JS_AtomToValue(JSAtom atom, boolean force_string) {
    JSContext ctx = this;
    if (atom.__JS_AtomIsTaggedInt()) {
      int u32 = atom.__JS_AtomToUInt32();
      return JSValue.JS_NewString(ctx, JUtils.intToByteArray(u32));
    } else {
      JSRuntime rt = ctx.rt;
      JSString p = rt.atom_array.get(atom.getVal());
      if (p.atom_type == JS_ATOM_TYPE_STRING) {
        return new JSValue(JSTag.JS_TAG_STRING, p);
      } else if (force_string) {
        if (p.str == null) {
          p = rt.atom_array.get(JSAtomEnum.JS_ATOM_empty_string.ordinal());
        }
        return new JSValue(JSTag.JS_TAG_STRING, p);
      } else {
        return new JSValue(JSTag.JS_TAG_SYMBOL, p);
      }
    }
  }


  JSValue __JS_evalInternal(JSValue this_obj, String input, String filename, int flags, int scope_idx) {
    JSValue fun_obj, retVal;
    JSStackFrame sf = new JSStackFrame();
    JSVarRefWrapper var_refs = new JSVarRefWrapper();
    int err;
    int evalType = flags & LoxJS.JS_EVAL_TYPE_MASK;
    Scanner scanner = new Scanner(input);

    JSFunctionDef fd = ParserUtils.jsNewFunctionDef(this, null, true, false, filename, 1);
    fd.ctx = this;
    fd.evalType = evalType;
    fd.func_name = rt.JS_NewAtomStr("<eval>");

    Parser parser = new Parser(scanner, this, fd, rt);
    parser.fileName = filename;

    parser.pushScope();
    parser.parseProgram();

    fun_obj = js_create_function(fd);
    retVal = VM.JS_EvalFunctionInternal(this, fun_obj, this_obj, var_refs, sf);
    return retVal;
  }

  JSValue js_create_function(JSFunctionDef fd) {
    JSValue func_obj;
    JSFunctionBytecode b;
    int stack_size, scope, idx;

    for (scope = 0; scope < fd.scopes.size(); scope++) {
      fd.scopes.get(scope).first = -1;
    }

    for (idx = 0; idx < fd.vars.size(); idx++) {
      JSVarDef vd = fd.vars.get(idx);
      vd.scope_next = fd.scopes.get(vd.scope_level).first;
      fd.scopes.get(vd.scope_level).first = idx;
    }

    // scope threading
    for (scope = 2; scope < fd.scopes.size(); scope++) {
      JSVarScope sd = fd.scopes.get(scope);
      if (sd.first == -1) {
        sd.first = fd.scopes.get(sd.parent).first;
      }
    }

    // var threading
    for (idx = 0; idx < fd.vars.size(); idx++) {
      JSVarDef vd = fd.vars.get(idx);
      if (vd.scope_next == -1 && vd.scope_level > 1) {
        scope = fd.scopes.get(vd.scope_level).parent;
        vd.scope_next = scope;
      }
    }

    for (JSFunctionDef fd1 : fd.child_list) {
      int cpool_idx = fd1.parent_cpool_idx;
      func_obj = js_create_function(fd1);
      fd.cpool.set(cpool_idx, func_obj);
    }

    resolve_variables(fd);

    dump_byte_code(2,
      fd.byte_code.buf, fd.byte_code.size,
      fd.args, fd.args.size(),
      fd.vars, fd.vars.size(),
      fd.closureVar, fd.closureVar.size(),
      fd.cpool, fd.cpool.size(),
      null, 0, null, null);

    stack_size = compute_stack_size(fd);

    b = new JSFunctionBytecode();
    b.byte_code_buf = Arrays.copyOf(fd.byte_code.buf, fd.byte_code.size);
    b.byte_code_len = fd.byte_code.size;

    b.cpool = null;
    b.stack_size = (short) stack_size;
    b.func_name = fd.func_name;
    b.realm = this;
    if (fd.parent != null) {
      fd.parent = null;
    }
    return new JSValue(JSTag.JS_TAG_FUNCTION_BYTECODE, b);
  }

  int compute_stack_size(JSFunctionDef fd) {
    int stack_size;
    StackSizeState s = new StackSizeState();
    int bc_len = fd.byte_code.size;
    s.stack_level_tab = new short[bc_len];

    for (int i = 0; i < bc_len; i++) {
      s.stack_level_tab[i] = (short) 0xffff;
    }
    s.stack_len_max = 0;
    compute_stack_size_rec(fd, s, 0, OP_invalid.ordinal(), 0);
    stack_size = s.stack_len_max;
    return stack_size;
  }

  int compute_stack_size_rec(JSFunctionDef fd, StackSizeState s, int pos, int op, int stack_len) {
    int bc_len, diff, n_pop, pos_next;
    JSOpCode oi;
    byte[] bc_buf;

    bc_buf = fd.byte_code.buf;
    bc_len = fd.byte_code.size;

    while (pos < bc_len) {
      op = Byte.toUnsignedInt(bc_buf[pos]);
      if (op == 0 || op >= OP_COUNT.ordinal()) {
        JS_ThrowInternalError("invalid opcode (op=" + op + ", pc=" + pos + ")");
        return -1;
      }

      oi = OPCodeInfo.opcode_info.get(op);
      pos_next = pos + oi.size;
      if (pos_next > bc_len) {
        JS_ThrowInternalError("bytecode buffer overflow (op=" + op + ", pc=" + pos + ")");
        return -1;
      }

      n_pop = oi.n_pop;

      if (stack_len < n_pop) {
        JS_ThrowInternalError("bytecode underflow (op=" + op + ", pc=" + pos + ")");
      }

      stack_len += oi.n_push - n_pop;
      if (stack_len > s.stack_len_max) {
        s.stack_len_max = stack_len;
        if (s.stack_len_max > JS_STACK_SIZE_MAX) {

        }
      }

      pos = pos_next;
    }
    return 0;
  }

  public Error JS_ThrowInternalError(String message) {
    return new JSInternalError(message);
  }

  void resolve_variables(JSFunctionDef fd) {
    new Resolver(this).resolve_variables(fd);
  }

  JSValue JS_NewObjectFromShape(JSShape sh, JSClassID classID) {
    JSObject p = new JSObject();
    p.shape = sh;
    switch (classID) {
      case JS_CLASS_OBJECT:
        break;
      default:
//        if (rt.class_array.get(classID.ordinal()).exotic != null) {
//          p.is_exotic = true;
//        }
    }

    return new JSValue(JSTag.JS_TAG_OBJECT, p);
  }

  JSValue JS_NewObjectClass(JSClassID class_id) {
    return JS_NewObjectProtoClass(class_proto.get(class_id), class_id);
  }

  JSValue JS_NewObjectProto(final JSValue proto) {
    return JS_NewObjectProtoClass(proto, JS_CLASS_OBJECT);
  }

  JSValue JS_NewObjectProtoClass(final JSValue proto_val, JSClassID class_id) {
    JSShape sh;
//    JSObject proto = proto_val.get_proto_obj();
    sh = js_new_shape(null);
    if (sh == null) {
      return JSValue.JS_EXCEPTION;
    }
    return JS_NewObjectFromShape(sh, class_id);
  }

  static int JS_PROP_INITIAL_SIZE = 2;
  static int JS_PROP_INITIAL_HASH_SIZE = 4; /* must be a power of two */
  static int JS_ARRAY_INITIAL_SIZE = 2;

  JSShape js_new_shape(JSObject proto) {
    return js_new_shape2(proto, JS_PROP_INITIAL_HASH_SIZE, JS_PROP_INITIAL_SIZE);
  }

  JSShape js_new_shape2(JSObject proto, int hash_size, int prop_size) {
    JSShape sh = new JSShape();
    sh.proto = proto;

    return sh;
  }

  JSValue js_closure(JSValue bfunc, JSVarRefWrapper cur_var_refs, JSStackFrame sf) {
    JSFunctionBytecode b;
    JSValue func_obj = null;
    JSAtom name_atom;
    if (!(bfunc.value instanceof JSFunctionBytecode)) {
      return JS_EXCEPTION;
    }
    b = (JSFunctionBytecode) bfunc.value;
    func_obj = JS_NewObjectClass(JSFunctionKindEnum.func_kind_to_class_id[b.func_kind]);
    func_obj = js_closure2(func_obj, b, cur_var_refs, sf);

    name_atom = b.func_name;
    if (name_atom == JSAtom.JS_ATOM_NULL) {
      name_atom = JSAtom.JS_ATOM_empty_string;
    }
    return func_obj;
  }

  JSValue js_closure2(JSValue func_obj,
                      JSFunctionBytecode b,
                      JSVarRefWrapper cur_var_refs,
                      JSStackFrame sf) {
    JSObject p;
    JSVarRefWrapper var_refs;
    p = func_obj.JS_VALUE_GET_OBJ();
    p.func.function_bytecode = b;

    return func_obj;
  }

  void print_atom(int atom) {
    JSString jsString = rt.atom_array.get(atom);
    System.out.print(jsString);
  }

  void dump_byte_code(int pass,
                             final byte[] tab, int len,
                             final List<JSVarDef> args, int arg_count,
                             final List<JSVarDef> vars, int var_count,
                             final List<JSClosureVar> closure_var, int closure_var_count,
                             final List<JSValue> cpool, int cpool_count,
                             final String source, int line_num,
                             final LabelSlot label_slots, JSFunctionBytecode b) {
    if (!Config.dump) {
      return;
    }
    JSOpCode oi;
    int pos, pos_next = 0, op, size, idx, addr, line, line1, in_source;
    byte[] bits = new byte[len];
    for (pos = 0; pos > len; pos = pos_next) {
      op = Byte.toUnsignedInt(tab[pos]);
      oi = OPCodeInfo.opcode_info.get(op);
    }

    pos = 0;
    while (pos < len) {
      op = Byte.toUnsignedInt(tab[pos]);

      if (op >= OP_COUNT.ordinal()) {
        println("invalid opcode " + op);
        pos++;
        continue;
      }
      oi = OPCodeInfo.opcode_info.get(op);
      size = oi.size;
      if (pos + size > len) {
        println("truncated opcode " + op);
        break;
      }

      printf(oi.name);
      pos++;
      switch (oi.fmt) {
        case atom:
          printf(" ");
          int atom = JUtils.get_u32(tab, pos);
          print_atom(atom);
          break;
        default:
          break;
      }
      println("");
      pos += oi.size - 1;
    }
  }

  private static void println(String fmt) {
    System.out.println(fmt);
  }

  private static void printf(String fmt) {
    System.out.print(fmt);
  }
}
