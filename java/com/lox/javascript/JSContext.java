package com.lox.javascript;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.clibrary.stdio_h.printf;
import static com.lox.clibrary.stdio_h.putchar;
import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSCFunction.*;
import static com.lox.javascript.JSCFunctionEnum.*;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSObject.*;
import static com.lox.javascript.JSPropertyUtils.resize_properties;
import static com.lox.javascript.JSShape.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;
import static com.lox.javascript.LoxJS.*;
import static com.lox.javascript.Parser.js_parse_program;
import static com.lox.javascript.Resolver.js_resolve_program;
import static com.lox.javascript.Resolver.push_scope;
import static com.lox.javascript.StackSizeState.compute_stack_size_rec;

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
  JSRefCountHeader header = new JSRefCountHeader();
  public JSValue global_obj;
  public JSValue global_var_obj;
  final JSRuntime rt;
  public int interrupt_counter;
  JSShape array_shape;
  JSValue function_proto;

  public JSContext(JSRuntime rt) {
    this.rt = rt;
    class_proto = new HashMap<>();
  }

  static void JS_AddIntrinsicBasicObjects(JSContext ctx) {
    JSValue proto;
    int i;

    ctx.class_proto.put(JS_CLASS_OBJECT, JS_NewObjectProto(ctx, JS_NULL));
    ctx.function_proto = JS_NewCFunction3(ctx, js_function_proto, "", 0,
      JS_CFUNC_generic, 0,
      ctx.class_proto.get(JS_CLASS_OBJECT));

    ctx.array_shape = js_new_shape2(ctx, JSValue.get_proto_obj(ctx.class_proto.get(JS_CLASS_ARRAY)),
      JS_PROP_INITIAL_HASH_SIZE, 1);
  }

  static void JS_AddIntrinsicBaseObjects(JSContext ctx) {
    ctx.global_obj = JS_NewObject(ctx);
    ctx.global_var_obj = JS_NewObjectProto(ctx, JS_NULL);
  }

  static int JS_SetGlobalVar(JSContext ctx, JSAtom prop, JSValue val, int flag) {
    JSObject p = ctx.global_var_obj.JS_VALUE_GET_OBJ();
    JSProperty pr;
    PJSProperty ppr = new PJSProperty();
    JSShapeProperty prs = find_own_property(ppr, p, prop);
    pr = ppr.val;
    int flags;

    if (prs != null) {
      if (flag != 1) {
        if (pr.u.value.JS_IsUninitialized()) {
          JSThrower.JS_ThrowReferenceErrorUninitialized(ctx, prs.atom);
          return -1;
        }
        if ((prs.flags & JS_PROP_WRITABLE) == 0) {
          return JSThrower.JS_ThrowTypeErrorReadOnly(ctx, JS_PROP_THROW, prop);
        }
      }
      set_value(ctx, pr.u.value, val);
      return 0;
    }

    flags = JS_PROP_THROW_STRICT;
    if (flag != 2 && is_strict_mode(ctx))
      flags |= JS_PROP_NO_ADD;
    return JS_SetPropertyInternal(ctx,  ctx.global_obj, prop, val, flags);
  }


   void set_value(JSValue pval, JSValue new_val)
  {
    pval.tag = new_val.tag;
    pval.value = new_val.value;
  }

  static void set_value(JSContext ctx, JSValue pval, JSValue new_val) {
    ctx.set_value(pval, new_val);
  }

   static JSValue JS_GetGlobalVar(JSContext ctx,
                                  JSAtom prop,
                                 int throw_ref_error)
  {
    JSObject p;
    JSShapeProperty prs;
    Pointer<JSProperty> ppr = new Pointer();
    JSProperty pr;

    /* no exotic behavior is possible in global_var_obj */
    p =  ctx.global_var_obj.JS_VALUE_GET_OBJ();
    prs = find_own_property(ppr, p, prop);
    pr = ppr.val;
    if (prs != null) {
      /* XXX: should handle JS_PROP_TMASK properties */
      if (pr.u.value.JS_IsUninitialized()) {
        return JSThrower.JS_ThrowReferenceErrorUninitialized(ctx, prs.atom);
      }
      return pr.u.value;
    }
    return JS_GetPropertyInternal(ctx, ctx.global_obj, prop,
      ctx.global_obj, throw_ref_error);
  }

  int JS_CheckDefineGlobalVar(JSAtom prop, int flags) {
    JSObject p;
    JSShapeProperty prs;
    p = global_obj.JS_VALUE_GET_OBJ();

    prs = find_own_property1(p, prop);
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
    prs = find_own_property1(p, prop);
    if (prs != null) {
      JSThrower.JS_ThrowSyntaxErrorVarRedeclaration(this, prop);
      return -1;
    }
    return 0;
  }

  int JS_DefineGlobalVar(JSContext ctx, JSAtom prop, int def_flags) {
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
    prs = find_own_property1(p, prop);
    if (prs != null)
      return 0;
    if (!p.extensible)
      return 0;
    pr = add_property(ctx, p, prop, flags);
    if (pr != null)
      return -1;
    pr.u.value = val;
    return 0;

  }


  static JSProperty add_property(JSContext ctx, JSObject p, JSAtom prop, int prop_flags) {
    JSShape sh, new_sh;

    sh = p.shape;

    if (add_shape_property(ctx, p.shape, p, prop, prop_flags) == 1) {
      return null;
    }

    return p.prop[p.shape.prop_count - 1];
  }

  static int add_shape_property(
    JSContext ctx, JSShape psh,
                         JSObject p, JSAtom atom, int prop_flags) {
    JSRuntime rt = ctx.rt;
    JSShape sh = psh;
    JSShapeProperty pr;
    JSShapeProperty[] prop;
    int hash_mask, new_shape_hash = 0;
    int h;

    if (sh.is_hashed) {
      js_shape_hash_unlink(rt, sh);
      new_shape_hash = shape_hash(shape_hash(sh.hash, atom.getVal()), prop_flags);
    }

    if (sh.prop_count >= sh.prop_size) {
      resize_properties(ctx, sh, p, sh.prop_count + 1);
    }

    /* Initialize the new shape property.
       The object property at p->prop[sh.prop_count] is uninitialized */
    if (sh.is_hashed) {
      sh.hash = new_shape_hash;
    }
    prop = sh.prop;
    pr = prop[sh.prop_count++];
    pr.atom = atom;
    pr.flags = prop_flags;
    sh.has_small_array_index |= __JS_AtomIsTaggedInt(atom);

    /* add in hash table */
    hash_mask = sh.prop_hash_mask;
    h = atom.getVal() & hash_mask;
    pr.hash_next = prop_hash_end(sh)[h];
    prop_hash_end(sh)[h] = sh.prop_count;
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

  static JSValue JS_AtomToString(JSContext ctx, JSAtom atom) {
    return ctx.__JS_AtomToValue(atom, true);
  }

  private JSValue __JS_AtomToValue(JSAtom atom, boolean force_string) {
    JSContext ctx = this;
    if (__JS_AtomIsTaggedInt(atom)) {
      int u32 = __JS_AtomToUInt32(atom);
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


  static JSValue __JS_evalInternal(JSContext ctx, JSValue this_obj, String input, String filename, int flags, int scope_idx) {
    JSValue fun_obj, retVal;
    JSStackFrame sf = new JSStackFrame();
    JSVarRefWrapper var_refs = new JSVarRefWrapper();
    int err;
    int evalType = flags & LoxJS.JS_EVAL_TYPE_MASK;
    Scanner scanner = new Scanner(input, ctx);

    JSFunctionDef fd = js_new_function_def(null, true, false, filename, 1);
    fd.ctx = ctx;
    fd.eval_type = evalType;
    fd.func_name = JS_ATOM__eval_.toJSAtom();

    Parser parser = new Parser(scanner, ctx, fd);
    parser.fileName = filename;
    js_parse_program(parser);
    fd.leave_line_number = parser.previous().line_num;

    Resolver s = new Resolver(ctx, fd);
    push_scope(s);
    err = js_resolve_program(s);
    if (err != 0) {

    }

    fun_obj = js_create_function(ctx, fd);
    retVal = VM.JS_EvalFunctionInternal(ctx, fun_obj, this_obj, var_refs, sf);
    return retVal;
  }

  static JSValue js_create_function(JSContext ctx, JSFunctionDef fd) {
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


    printf("pass 1\n");
    Dumper.dump_byte_code(ctx, 1,
      fd.byte_code.buf, fd.byte_code.size,
      fd.args, fd.args.size(),
      fd.vars, fd.vars.size(),
      fd.closure_var, fd.closure_var.size(),
      fd.cpool, fd.cpool.size(),
      fd.source, fd.line_num,
      fd.label_slots.toArray(new LabelSlot[0]), null);
    printf("\n");
    IRResolver.resolve_variables(ctx, fd);
    printf("pass 2\n");
    Dumper.dump_byte_code(ctx, 2,
      fd.byte_code.buf, fd.byte_code.size,
      fd.args, fd.args.size(),
      fd.vars, fd.vars.size(),
      fd.closure_var, fd.closure_var.size(),
      fd.cpool, fd.cpool.size(),
      fd.source, fd.line_num,
      fd.label_slots.toArray(new LabelSlot[0]), null);
    printf("\n");
    LabelSlot.resolve_labels(ctx, fd);

    stack_size = compute_stack_size(ctx, fd);

    b = new JSFunctionBytecode();
    b.byte_code_buf = Arrays.copyOf(fd.byte_code.buf, fd.byte_code.size);
    b.byte_code_len = fd.byte_code.size;

    b.cpool = new JSValue[fd.cpool.size()];
    b.cpool_count = fd.cpool.size();
    for (i = 0; i < b.cpool_count; i++) {
      b.cpool[i] = fd.cpool.get(i);
    }
    b.closure_var = new JSClosureVar[0];
    b.stack_size = (short) stack_size;

    if ((fd.js_mode & JS_MODE_STRIP) != 0) {
      fd.filename = null;
      fd.pc2line = null;
    } else {
      b.has_debug = true;
      b.debug.filename = fd.filename;
      b.debug.line_num = fd.line_num;

      b.debug.pc2line_buf = fd.pc2line.buf;
      b.debug.pc2line_len = fd.pc2line.size;
      b.debug.source = fd.source;
      b.debug.source_len = fd.source_len;
    }
    b.func_name = fd.func_name;

    int arg_count = fd.args.size();
    int var_count = fd.vars.size();
    int total_count = arg_count + var_count;
    if (total_count > 0) {
      b.vardefs = new JSVarDef[total_count];
      b.args = new JSVarDef[arg_count];
      b.local_vars = new JSVarDef[var_count];
      for (i = 0; i < fd.args.size(); i++) {
        b.vardefs[i] = fd.args.get(i);
        b.args[i] = fd.args.get(i);
      }
      for (i = 0; i < fd.vars.size(); i++) {
        b.vardefs[arg_count + i] = fd.vars.get(i);
        b.local_vars[ i] = fd.vars.get(i);
      }
    }
    b.var_count = fd.vars.size();
    b.arg_count = fd.args.size();
    b.defined_arg_count = fd.defined_arg_count;


    b.realm = ctx;

    if ((fd.js_mode & JS_MODE_STRIP) == 0) {
      Dumper.js_dump_function_bytecode(ctx, b);
    }
    if (fd.parent != null) {
      fd.parent = null;
    }
    return new JSValue(JSTag.JS_TAG_FUNCTION_BYTECODE, b);
  }

  static int compute_stack_size(JSContext ctx, JSFunctionDef fd) {
    int stack_size;
    StackSizeState s = new StackSizeState();
    int bc_len = fd.byte_code.size;
    s.stack_level_tab = new short[bc_len];

    for (int i = 0; i < bc_len; i++) {
      s.stack_level_tab[i] = (short) 0xffff;
    }
    s.stack_len_max = 0;
    compute_stack_size_rec(ctx, fd, s, 0, OPCodeEnum.OP_invalid.ordinal(), 0);
    stack_size = s.stack_len_max;
    return stack_size;
  }

  static JSValue JS_NewObjectFromShape(JSContext ctx, JSShape sh, JSClassID classID) {
    JSObject p = new JSObject();
    p.shape = sh;
    p.prop = new JSProperty[sh.prop_size];
    for (int i = 0 ; i < sh.prop_size; i++) {
      p.prop[i] = new JSProperty();
    }
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

  static JSValue JS_NewArray(JSContext ctx)
  {
    return JS_NewObjectFromShape(ctx, js_dup_shape(ctx.array_shape),
      JS_CLASS_ARRAY);
  }

  static JSValue JS_NewObjectClass(JSContext ctx, JSClassID class_id) {
    return JS_NewObjectProtoClass(ctx, ctx.class_proto.get(class_id), class_id);
  }

  static JSValue JS_NewObjectProto(JSContext ctx, final JSValue proto) {
    return JS_NewObjectProtoClass(ctx, proto, JS_CLASS_OBJECT);
  }

  static JSValue JS_NewObject(JSContext ctx) {
    return JS_NewObjectProtoClass(ctx, ctx.class_proto.get(JS_CLASS_OBJECT), JS_CLASS_OBJECT);
  }

  static JSValue JS_NewObjectProtoClass(JSContext ctx, final JSValue proto_val, JSClassID class_id) {
    JSShape sh;
//    JSObject proto = get_proto_obj(proto_val);
    sh = js_new_shape(ctx, null);
    if (sh == null) {
      return JSValue.JS_EXCEPTION;
    }
    return JS_NewObjectFromShape(ctx, sh, class_id);
  }

  static int JS_PROP_INITIAL_SIZE = 2;
  static int JS_PROP_INITIAL_HASH_SIZE = 4; /* must be a power of two */
  static int JS_ARRAY_INITIAL_SIZE = 2;

  static JSShape js_new_shape(JSContext ctx, JSObject proto) {
    return js_new_shape2(ctx, proto, JS_PROP_INITIAL_HASH_SIZE, JS_PROP_INITIAL_SIZE);
  }

  static JSShape js_new_shape2(JSContext ctx, JSObject proto, int hash_size, int prop_size) {
    JSShape sh = new JSShape();

    sh.hash_array = new int[hash_size];
    sh.prop = new JSShapeProperty[prop_size];
    for (int i  = 0; i < prop_size; i++) {
      sh.prop[i] = new JSShapeProperty();
    }
    sh.proto = proto;

    sh.prop_hash_mask = hash_size - 1;
    sh.prop_size = prop_size;
    sh.prop_count = 0;
    sh.deleted_prop_count = 0;

    sh.hash = shape_initial_hash(proto);
    sh.is_hashed = true;
    sh.has_small_array_index = false;

    return sh;
  }

  static int shape_hash(int h, int val)
  {
    return (h + val) * 0x9e370001;
  }

  /* truncate the shape hash to 'hash_bits' bits */
  static int get_shape_hash(int h, int hash_bits)
  {
    return h >> (32 - hash_bits);
  }

  static int shape_initial_hash(JSObject proto)
  {
    int h;
    int hash = proto == null ? 0 : proto.hashCode();
    h = shape_hash(1, hash);
    return h;
  }

  static JSValue js_closure(JSContext ctx, JSValue bfunc, JSVarRefWrapper cur_var_refs, JSStackFrame sf) {
    JSFunctionBytecode b;
    JSValue func_obj = null;
    JSAtom name_atom;
    if (!(bfunc.value instanceof JSFunctionBytecode)) {
      return JSValue.JS_EXCEPTION;
    }
    b = (JSFunctionBytecode) bfunc.value;
    func_obj = JS_NewObjectClass(ctx, JSFunctionKindEnum.func_kind_to_class_id[b.func_kind]);
    func_obj = js_closure2(ctx, func_obj, b, cur_var_refs, sf);

    name_atom = b.func_name;
    if (name_atom == JSAtom.JS_ATOM_NULL) {
      name_atom = JSAtom.JS_ATOM_empty_string;
    }
    return func_obj;
  }

  static JSValue js_closure2(JSContext ctx, JSValue func_obj,
                      JSFunctionBytecode b,
                      JSVarRefWrapper cur_var_refs,
                      JSStackFrame sf) {
    JSObject p;
    JSVarRefWrapper var_refs;
    p = func_obj.JS_VALUE_GET_OBJ();
    p.u.func.function_bytecode = b;

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
    return JS_AtomGetStr(rt, atom.getVal());
  }

  static String JS_AtomGetStr(JSRuntime rt, JSAtom atom) {
    return JS_AtomGetStr(rt, atom.getVal());
  }

  static String JS_AtomGetStr(JSRuntime rt, int atom) {
    JSString jsString = rt.atom_array.get(atom);
    return jsString.str;
  }

  static String JS_AtomGetStrRT(JSRuntime rt,
                                     JSAtom atom)
  {
    return JS_AtomGetStr(rt, atom);
  }

  static JSFunctionDef js_new_function_def(JSContext ctx, JSFunctionDef parent,
                                           boolean isEval, boolean isFuncExpr, String filename, int line_num) {
    return js_new_function_def(parent, isEval, isFuncExpr, filename, line_num);
  }

  static JSFunctionDef js_new_function_def(JSFunctionDef parent,
                                           boolean isEval, boolean isFuncExpr, String filename, int line_num) {
    JSFunctionDef fd = new JSFunctionDef(parent, isEval, isFuncExpr, filename, line_num);

    fd.line_num = 1;
    fd.scope_level = 0;
    fd.scope_first = -1;
    JSVarScope varScope = new JSVarScope();
    fd.scopes.add(varScope);
    fd.scopes.get(0).first = -1;
    fd.scopes.get(0).parent = -1;
    fd.filename = filename;
    fd.pc2line = new DynBuf();
    fd.last_opcode_line_num = line_num;
    return fd;
  }


  static boolean is_strict_mode(JSContext ctx) {
    JSStackFrame sf = ctx.rt.current_stack_frame;
    return false;
  }

  static JSContext JS_DupContext(JSContext ctx)
  {
    ctx.header.ref_count++;
    return ctx;
  }
}
