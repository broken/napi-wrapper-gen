/**
 * A C++ header grammar that only analyzes information that would be relevant to
 * a node-webkit wrapper class.
 */
grammar napiwrapper;

header: body;

body
  :  namespace body
  |  classDeclaration body
  |  enumDeclaration body
  |  usingDeclaration body
  |  cppClass
  |
  ;

namespace: NAMESPACE Identifier LCBRACE body RCBRACE;
classDeclaration: CLASS Identifier SEMICOLON;
enumDeclaration: ENUM Identifier LCBRACE (Identifier COMMA?)* RCBRACE SEMICOLON;

usingDeclaration: USING NAMESPACE Identifier SEMICOLON;

classBlock: publicBlock* ;

publicBlock: constructor | destructor | method | friend | opMethod ;

constructor: EXPLICIT? Identifier parameterList SEMICOLON;
destructor: VIRTUAL? '~' Identifier LPAREN RPAREN SEMICOLON;
method: template? STATIC? VIRTUAL? type Identifier parameterList CONST? (block | SEMICOLON);
friend: FRIEND CLASS? type SEMICOLON;
opMethod: type ( OPEQ | OPLT | OPGT ) parameterList CONST? SEMICOLON ;
template: TEMPLATE LT TYPENAME Identifier GT;

parameterList: LPAREN (parameter (COMMA parameter)*)? RPAREN;
parameter: type Identifier (EQUALS (type (LPAREN RPAREN)? | Number | EmptyBlock | Text))?;

cppClass: CLASS Identifier LCBRACE classBlock RCBRACE SEMICOLON;

block: LCBRACE innerBlock RCBRACE;
innerBlock: (Identifier | SEMICOLON | block | type | STATIC | EQUALS | Number)*;

type: CONST? Identifier generic? Modifier*;
fnType: Identifier LPAREN typeList RPAREN;
generic: LT (typeList | fnType) GT;
typeList: type (COMMA type)?;

COMMA: ',';
LPAREN: '(';
RPAREN: ')';
LCBRACE: '{';
RCBRACE: '}';
LT: '<';
GT: '>';
SEMICOLON: ';';
EQUALS: '=';
DOUBLE_QUOTE: '"';

FRIEND: 'friend' ;
NAMESPACE: 'namespace' ;
USING: 'using' ;
CLASS: 'class' ;
CONST: 'const' ;
ENUM: 'enum';
EXPLICIT: 'explicit' ;
STATIC: 'static' ;
TEMPLATE: 'template';
TYPENAME: 'typename';
VIRTUAL: 'virtual' ;
OPEQ: 'operator=' ;
OPLT: 'operator<' ;
OPGT: 'operator>' ;

fragment STAR: '*';
fragment AMPERSAND: '&';


Text: DOUBLE_QUOTE ( Nondigit | Digit )* DOUBLE_QUOTE;
NamespacePrefix: IdentifierName '::' (NamespacePrefix)?;
Identifier: NamespacePrefix? IdentifierName;
fragment IdentifierName: Nondigit ( Nondigit | Digit )*;

Modifier: STAR | AMPERSAND;

Number: Digit+;
EmptyBlock: LCBRACE RCBRACE;



PrivateBlock: 'private:' ~[}]*
    -> skip ;

ProtectedBlock: 'protected:' ~[}]*
    -> skip ;

Public: 'public:'
    -> skip ;

Directive: '#' ~[\r\n]*
    -> skip ;

WS: [ \t]+
    -> skip ;

Newline: ( '\r' '\n'?
        |   '\n'
        )
    -> skip ;

BlockComment: '/*' .*? '*/'
    -> skip ;

LineComment: '//' ~[\r\n]*
    -> skip ;

fragment Nondigit: [a-zA-Z_];
fragment Digit: [0-9];

Struct: 'struct ' .*? '};'
    -> skip ;
