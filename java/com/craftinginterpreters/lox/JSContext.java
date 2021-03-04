package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

import static com.craftinginterpreters.lox.JSClassID.JS_CLASS_OBJECT;
import static com.craftinginterpreters.lox.JSValue.JS_EXCEPTION;

/**
 * @author benpeng.jiang
 * @title: JSContext
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/222:00 PM
 */
public class JSContext {
  private static Interpreter interpreter;
  final Map<JSClassID, JSValue> class_proto;

  public JSValue globalObj;
  final JSRuntime rt;

  public JSContext(JSRuntime rt) {
    this.rt = rt;
    class_proto = new HashMap<>();
  }

  JSValue __JS_evalInternal(JSContext ctx, JSValue this_obj, String input, String filename, int flags, int scope_idx) {
    JSValue fun_obj, retVal;
    JSStackFrame sf = new JSStackFrame();
    JSVarRefWrapper var_refs = new JSVarRefWrapper();
    int err;
    int evalType = flags & LoxJS.JS_EVAL_TYPE_MASK;
    Scanner scanner = new Scanner(input);

    JSFunctionDef fd = ParserUtils.jsNewFunctionDef(ctx, null, true, false, filename, 1);
    fd.evalType = evalType;
    fd.funcName = "<eval>";

    Parser parser = new Parser(scanner, this, fd, ctx.rt);
    parser.fileName = filename;

    parser.pushScope();
    parser.parseProgram();

    interpreter = new Interpreter(ctx);
    Resolver resolver = new Resolver(interpreter);
    resolver.visitFunctionStmt(fd);

    fun_obj = js_create_function(fd);
    retVal = JSVM.JS_EvalFunctionInternal(this, fun_obj, this_obj, var_refs, sf);
    return retVal;
  }

  JSValue js_create_function(JSFunctionDef fd) {
    JSValue func_obj;
    JSFunctionByteCode b;

    return func_obj;
  }

  JSValue JS_NewObjectFromShape(JSShape sh, JSClassID classID) {
    JSObject p = new JSObject();
    p.shape = sh;
    switch (classID) {
      case JS_CLASS_OBJECT:
        break;
      default:
        if (rt.class_array.get(classID.ordinal()).exotic != null) {
          p.is_exotic = true;
        }
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
    JSObject proto = proto_val.get_proto_obj();
    sh = js_new_shape(proto);
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
    JSFunctionByteCode b;
    JSValue func_obj = null;
    JSAtom name_atom;
    if (!(bfunc.value instanceof JSFunctionByteCode)) {
      return JS_EXCEPTION;
    }
    b = (JSFunctionByteCode) bfunc.value;
    func_obj = JS_NewObjectClass(JSFunctionKindEnum.func_kind_to_class_id[b.func_kind]);
    name_atom = b.func_name;
    if (name_atom == JSAtom.JS_ATOM_NULL) {
      name_atom = JSAtom.JS_ATOM_empty_string;
    }
    return func_obj;
  }
}
