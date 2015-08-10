grammar ExtendedExpression;

@header{
	package net.unicoen.parser;
}

program :
		classModifiers CLASS name LEFTBRACE methodDeclaration* RIGHTBRACE EOF;
classModifiers:
		classVisibility? abs? stat?;
classVisibility:
		PUBLIC | PRIVATE;
abs:
		ABSTRACT;
stat:
		STATIC;
methodDeclaration:
		methodModifiers type name LEFTPAREN methodArguments? RIGHTPAREN methodBody;
methodModifiers:
		methodVisibility? fin? abs? stat?;
fin:
		FINAL;
methodVisibility:
		PUBLIC | PROTECTED | PACKAGE | PRIVATE;
methodArguments:
		methodArgument ( COMMA methodArgument ) *;
methodArgument:
		type name;
methodBody:
		LEFTBRACE statement* RIGHTBRACE;
name:
		ID+;
type:
		VOID | INT | DOUBLE | TURTLE;
statement:
		ifStatement | variableDeclaration | whileStatement | setterStatement | methodCallStatement ;
variableDeclaration:
		type name EQUAL expression SEMICOLON | type name SEMICOLON;
ifStatement :
		IF LEFTPAREN compareExp RIGHTPAREN LEFTBRACE statement* RIGHTBRACE (  ELSE LEFTBRACE RIGHTBRACE ) ? ;
whileStatement :
		WHILE LEFTPAREN compareExp RIGHTPAREN LEFTBRACE statement* RIGHTBRACE;
setterStatement :
		identifier EQUAL expression SEMICOLON;
methodCallStatement :
		methodCallExpr SEMICOLON ;
expression :
		compareExp | normalExp | methodCallExpr;
compareExp :
		normalExp compareOp normalExp ;
compareOp :
		LARGER | LARGEROREQUALS | EQUALS | SMALLEROREQUALS | SMALLER ;
normalExp :
		term (  addSubOp term ) * ;
methodCallExpr :
		( identifier DOT ) * identifier LEFTPAREN methodParam ? RIGHTPAREN ;
methodParam :
		normalExp ( COMMA normalExp ) * ;
term :
		factor (  mulDivOp factor ) * ;
factor :
		number | LEFTPAREN normalExp RIGHTPAREN | identifier;
identifier :
		name ;
number :
		integer | decimal ;
integer :
		ZERO | NONZERODIGIT digit* ;
decimal :
		NONZERODIGIT digit* DOT digit* | ZERO DOT digit* ;
mulDivOp :
		MUL | DIV ;
addSubOp :
		ADD | SUB ;
digit :
		ZERO | NONZERODIGIT ;
LEFTPAREN :
		'(' ;
RIGHTPAREN :
		')' ;
ZERO :
		'0' ;
NONZERODIGIT :
		'1' |'2' |'3' |'4' |'5' |'6' |'7' |'8' |'9' ;
ADD :
		'+' ;
SUB :
		'-' ;
INC :
		'++' ;
DEC :
		'--' ;
MUL :
		'*' ;
DIV :
		'/' ;
DOT :
		'.' ;
LARGER :
		'>' ;
LARGEROREQUALS :
		'>=' ;
EQUALS :
		'==' ;
SMALLEROREQUALS :
		'<=' ;
SMALLER :
		'<' ;
IF :
		'if' ;
ELSE :
		'else' ;
LEFTBRACE :
		'{' ;
RIGHTBRACE :
		'}' ;
ID :
		[a-zA-Z] ;
SEMICOLON :
		';' ;
CLASS :
		'class';
VOID:
		'void';
EQUAL:
		'=';
INT:
		'int';
DOUBLE:
		'double';
PUBLIC:
		'public';
PRIVATE:
		'private';
ABSTRACT:
		'abstract';
PROTECTED:
		'protected';
PACKAGE:
		'package';
STATIC:
		'static';
FINAL:
		'final';
COMMA:
		',';
WHILE:
		'while';
WS :
		[ \t\r\n\u000C]+ -> skip;
TURTLE:
		'Turtle';