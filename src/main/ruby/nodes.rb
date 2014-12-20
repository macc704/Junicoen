require_relative 'dsl.rb'
require_relative 'writer.rb'

Dsl.define_node do |x|
  x.package = "net.unicoen.node"
  x.prefix = "Uni"

  x.node "Node", interface: true, generics: :self do

    x.node "Expr", abstract: true, generics: :self do
      #
      # Factor
      #
      {
        "Bool" => :boolean,
        "Int" => :int,
        "String" => String
      }.each do |name, type|
        x.node "#{name}Literal" do |d|
          d.mem "value", type
        end
      end
      x.node "Ident" do |d|
        d.mem "name", String
      end
      #
      # Expressions
      #
      x.node "MethodCall" do |d|
        d.mem "receiver", "Expr"
        d.mem "methodName", String
        d.mem "args", "Expr", list: true
      end
      x.node "UnaryOp" do |d|
        d.mem "operator", String
        d.mem "expr", "Expr"
      end
      x.node "BinOp" do |d|
        d.mem "operator", String
        d.mem "left", "Expr"
        d.mem "right", "Expr"
      end
      x.node "CondOp" do |d|
        d.mem "cond", "Expr"
        d.mem "trueExpr", "Expr"
        d.mem "falseExpr", "Expr"
      end
      #
      # Block
      #
      x.node "If" do |d|
        d.mem "cond", "Expr"
        d.mem "trueBlock",  "Expr", list: true
        d.mem "falseBlock", "Expr", list: true
      end
      x.node "For" do |d|
        d.mem "init", "Expr"
        d.mem "cond", "Expr"
        d.mem "step", "Expr"
        d.mem "body", "Expr", list: true
      end
      x.node "While" do |d|
        d.mem "cond", "Expr"
        d.mem "body", "Expr", list: true
      end
      x._node "Try" do |d|
        d.mem "tryExpr", "Expr", list: true
        d.mem "tryBlock", "Expr", list: true
        d.mem "catchBlock", "Expr", list: true
        d.mem "finallyBlock", "Expr", list: true
      end
      x._node "DecVar" do |d|
        d.mem "modifiers", String, list: true
        d.mem "type", String
        d.mem "name", String
      end
    end
  
    x.node "MemberDec", abstract: true, generics: :self do
      x.node "FuncDec" do |d|
        d.mem "funcName", String
        d.mem "modifiers", String, list: true
        d.mem "returnType", String
        d.mem "args", "Arg", list: true
        d.mem "body", "Expr", list: true
      end
      x.node "Arg", nil do |d|
        d.mem "type", String
        d.mem "name", String
      end
    end
    x.node "ClassDec" do |d|
      d.mem "className", String
      d.mem "modifiers", String, list: true
      d.mem "members", "MemberDec", list: true
    end
  end
end
