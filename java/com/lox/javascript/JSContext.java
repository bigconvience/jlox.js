package com.lox.javascript;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.clibrary.stdio_h.printf;
import static com.lox.clibrary.stdio_h.putchar;
import static com.lox.javascript.JSAtom.JS_ATOM_TYPE_STRING;
import static com.lox.javascript.JSClassID.JS_CLASS_OBJECT;
import static com.lox.javascript.JS_PROP.*;
import static com.lox.javascript.LoxJS.JS_MODE_STRIP;

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

  public JSValue global_obj;
  public JSValue global_var_obj;
  final JSRuntime rt;

  public JSContext(JSRuntime rt) {
    this.rt = rt;
    class_proto = new HashMap<>();
  }

  void JS_AddIntrinsicBasicObjects() {

  }

  void JS_AddIntrinsicBaseObjects() {
    global_obj = JS_NewObject();
    global_var_obj = JS_NewObjectProto(JSValue.JS_NULL);
  }

  int JS_SetGlobalVar(JSAtom prop, JSValue val, int flag) {
    JSObject p = global_var_obj.JS_VALUE_GET_OBJ();
    JSProperty.Ptr pr = new JSProperty.Ptr();
    JSShapeProperty prs = p.find_own_property(pr, prop);
    int flags;

    if (prs != null) {
      if (flag != 1) {
        if (pr.value().JS_IsUninitialized()) {
          JSThrower.JS_ThrowReferenceErrorUninitialized(this, prs.atom);
          return -1;
        }
        if ((prs.flags & JS_PROP_WRITABLE) == 0) {
          return JSThrower.JS_ThrowTypeErrorReadOnly(this, JS_PROP_THROW, prop);
        }
      }
      set_value(pr.value(), val);
      return 0;
    }

    flags = JS_PROP_THROW_STRICT;
    if (flag != 2 && is_strict_mode())
      flags |= JS_PROP_NO_ADD;
    return global_obj.JS_SetPropertyInternal(this,  prop, val, flags);
  }


   void set_value(JSValue pval, JSValue new_val)
  {
    pval.tag = new_val.tag;
    pval.value = new_val.value;
  }

  static void set_value(JSContext ctx, JSValue pval, JSValue new_val) {
    ctx.set_value(pval, new_val);
  }

   JSValue JS_GetGlobalVar(JSAtom prop,
                                 int throw_ref_error)
  {
    JSObject p;
    JSShapeProperty prs;
    JSProperty.Ptr pr = new JSProperty.Ptr();

    /* no exotic behavior is possible in global_var_obj */
    p =  global_var_obj.JS_VALUE_GET_OBJ();
    prs = p.find_own_property(pr,  prop);
    if (prs != null) {
      /* XXX: should handle JS_PROP_TMASK properties */
      if (pr.value().JS_IsUninitialized()) {
        return JSThrower.JS_ThrowReferenceErrorUninitialized(this, prs.atom);
      }
      return pr.value();
    }
    return global_obj.JS_GetPropertyInternal(this, prop,
      global_obj, throw_ref_error);
  }

  int JS_CheckDefineGlobalVar(JSAtom prop, int flags) {
    JSObject p;
    JSShapeProperty prs;
    p = global_obj.JS_VALUE_GET_OBJ();

    prs = p.find_own_property1(prop);
    if ((flags & DEFINE_GLOBAL_LEX_VAR) != 0) {
      if (prs != null && (prs.flags & JS_PROP.JS_PROP_CONFIGURABLE) == 0) {
        JSThrower.JS_ThrowSyntaxErrorVarRedeclaration(this, prop);
        return -1;
      }
    } else {
      if (prs != null && !p.extensible) {
        JSThrower.JS_ThrowSyntaxErrorVarRedeclaration(this, prop);
        return -1;
      }
      if ((flags & DEFINE_GLOBAL_FUNC_VAR) != 0) {
        if (prs != null) {

        }
      }
    }
    p = global_var_obj.JS_VALUE_GET_OBJ();
    prs = p.find_own_property1(prop);
    if (prs != null) {
      JSThrower.JS_ThrowSyntaxErrorVarRedeclaration(this, prop);
      return -1;
    }
    return 0;
  }

  int JS_DefineGlobalVar(JSAtom prop, int def_flags) {
    JSObject p;
    JSShapeProperty prs;
    JSProperty pr;
    JSValue val;
    int flags;

    if ((def_flags & DEFINE_GLOBAL_LEX_VAR) != 0) {
      p = global_var_obj.JS_VALUE_GET_OBJ();
      flags = JS_PROP.JS_PROP_ENUMERABLE | (def_flags & JS_PROP_WRITABLE) |
        JS_PROP.JS_PROP_CONFIGURABLE;
      val = JSValue.JS_UNINITIALIZED;
    } else {
      p = global_obj.JS_VALUE_GET_OBJ();
      flags = JS_PROP.JS_PROP_ENUMERABLE | JS_PROP_WRITABLE |
        (def_flags & JS_PROP.JS_PROP_CONFIGURABLE);
      val = JSValue.JS_UNDEFINED;
    }
    prs = p.find_own_property1(prop);
    if (prs != null)
      return 0;
    if (!p.extensible)
      return 0;
    pr = add_property(p, prop, flags);
    if (pr != null)
      return -1;
    pr.value = val;
    return 0;

  }


  JSProperty add_property(JSObject p, JSAtom prop, int prop_flags) {
    JSShape sh, new_sh;

    sh = p.shape;

    if (add_shape_property(p.shape, p, prop, prop_flags) == 1) {
      return null;
    }

    return p.prop.get(p.shape.prop.size() - 1);
  }

  int add_shape_property(JSShape psh,
                         JSObject p, JSAtom atom, int prop_flags) {

    JSShape sh = psh;
    JSShapeProperty pr;
    List<JSShapeProperty> prop;
    int hash_mask, new_shape_hash = 0;

    if (p.prop.size() <= sh.prop.size()) {
      resize_properties(sh, p, sh.prop.size() + 1);
    }

    /* Initialize the new shape property.
       The object property at p->prop[sh->prop_count] is uninitialized */
    prop = sh.get_shape_property();
    pr = new JSShapeProperty();
    prop.add(pr);
    pr.atom = atom;
    pr.flags = prop_flags;
    return 0;
  }

  int resize_properties(JSShape psh,
                        JSObject p, int count) {
    int start = p.prop.size();
    while (start < count) {
      p.prop.add(new JSProperty());
      start++;
    }
    return 0;
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
    Scanner scanner = new Scanner(input, this);

    JSFunctionDef fd = js_new_function_def(null, true, false, filename, 1);
    fd.ctx = this;
    fd.eval_type = evalType;
    fd.func_name = rt.JS_NewAtomStr("<eval>");

    Parser parser = new Parser(scanner, this, fd, rt);
    parser.fileName = filename;
    parser.parse_program();

    fun_obj = js_create_function(this, fd);
    retVal = VM.JS_EvalFunctionInternal(this, fun_obj, this_obj, var_refs, sf);
    return retVal;
  }

  JSValue js_create_function(JSContext ctx, JSFunctionDef fd) {
    JSValue func_obj;
    JSFunctionBytecode b;
    int stack_size, scope, idx;
    int i;

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
      func_obj = js_create_function(ctx, fd1);
      fd.cpool.set(cpool_idx, func_obj);
    }

    Resolver s = new Resolver(ctx, fd);
    Resolver.push_scope(s);
    ast_2_opcode(s, fd);
    printf("pass 1\n");
    Dumper.dump_byte_code(this, 1,
      fd.byte_code.buf, fd.byte_code.size,
      fd.args, fd.args.size(),
      fd.vars, fd.vars.size(),
      fd.closure_var, fd.closure_var.size(),
      fd.cpool, fd.cpool.size(),
      "", fd.line_number,
      fd.label_slots.toArray(new LabelSlot[0]), null);
    printf("\n");
    IRResolver.resolve_variables(this, fd);
    printf("pass 2\n");
    Dumper.dump_byte_code(this, 2,
      fd.byte_code.buf, fd.byte_code.size,
      fd.args, fd.args.size(),
      fd.vars, fd.vars.size(),
      fd.closure_var, fd.closure_var.size(),
      fd.cpool, fd.cpool.size(),
      "", fd.line_number,
      fd.label_slots.toArray(new LabelSlot[0]), null);
    printf("\n");
    LabelSlot.resolve_labels(this, fd);

    stack_size = compute_stack_size(fd);

    b = new JSFunctionBytecode();
    b.byte_code_buf = Arrays.copyOf(fd.byte_code.buf, fd.byte_code.size);
    b.byte_code_len = fd.byte_code.size;

    b.cpool = new JSValue[0];
    b.closure_var = new JSClosureVar[0];
    b.stack_size = (short) stack_size;

    b.func_name = fd.func_name;

    int arg_count = fd.args.size();
    int var_count = fd.vars.size();
    int total_count = arg_count + var_count;
    if (total_count > 0) {
      b.vardefs = new JSVarDef[total_count];
      for (i = 0; i < fd.args.size(); i++) {
        b.vardefs[i] = fd.args.get(i);
      }
      for (i = 0; i < fd.vars.size(); i++) {
        b.vardefs[arg_count + i] = fd.vars.get(i);
      }
    }
    b.var_count = fd.vars.size();
    b.arg_count = fd.args.size();
    b.defined_arg_count = fd.defined_arg_count;


    b.realm = this;

    if ((fd.js_mode & JS_MODE_STRIP) == 0) {
      Dumper.js_dump_function_bytecode(this, b);
    }
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
    compute_stack_size_rec(fd, s, 0, OPCodeEnum.OP_invalid.ordinal(), 0);
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
      if (op == 0 || op >= OPCodeEnum.OP_COUNT.ordinal()) {
        JSThrower.JS_ThrowInternalError(this, "invalid opcode (op=" + op + ", pc=" + pos + ")");
        return -1;
      }

      oi = OPCodeInfo.opcode_info.get(op);
      pos_next = pos + oi.size;
      if (pos_next > bc_len) {
        JSThrower.JS_ThrowInternalError(this, "bytecode buffer overflow (op=" + op + ", pc=" + pos + ")");
        return -1;
      }

      n_pop = oi.n_pop;

      if (stack_len < n_pop) {
        JSThrower.JS_ThrowInternalError(this, "bytecode underflow (op=" + op + ", pc=" + pos + ")");
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

  void ast_2_opcode(Resolver s, JSFunctionDef fd) {
    s.resolve();
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

  JSValue JS_NewObject() {
    return JS_NewObjectProtoClass(class_proto.get(JS_CLASS_OBJECT), JS_CLASS_OBJECT);
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
      return JSValue.JS_EXCEPTION;
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

    char[] p = jsString.str.toCharArray();
    int i;
    for (i = 0; i < p.length; i++) {
      int c = 0xFF&p[i];
      if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
        (c == '_' || c == '$') || (c >= '0' && c <= '9' && i > 0)))
        break;
    }
    if (i > 0 && i == p.length) {
      printf("%s", jsString.toString());
    } else {
      putchar('"');
      printf(i, p);
      for (; i < p.length; i++) {
        int c = 0xFF&p[i];
        if (c == '\"' || c == '\\') {
          putchar('\\');
          putchar(c);
        } else if (c >= ' ' && c <= 126) {
          putchar(c);
        } else if (c == '\n') {
          putchar('\\');
          putchar('n');
        } else {
          printf("\\u%04x", c);
        }
      }
      putchar('\"');
    }
  }

  void print_atom(JSAtom atom) {
    print_atom(atom.getVal());
  }

  String JS_AtomGetStr(JSAtom atom) {
    return JS_AtomGetStr(atom.getVal());
  }

  String JS_AtomGetStr(int atom) {
    JSString jsString = rt.atom_array.get(atom);
    return jsString.str;
  }

  static JSFunctionDef js_new_function_def(JSFunctionDef parent,
                                           boolean isEval, boolean isFuncExpr, String filename, int lineNum) {
    JSFunctionDef fd = new JSFunctionDef(parent, isEval, isFuncExpr, filename, lineNum);

    fd.scope_level = 0;
    fd.scope_first = -1;
    JSVarScope varScope = new JSVarScope();
    fd.scopes.add(varScope);
    fd.scopes.get(0).first = -1;
    fd.scopes.get(0).parent = -1;
    return fd;
  }


  boolean is_strict_mode() {
    JSStackFrame sf = rt.current_stack_frame;
    return false;
  }
}
