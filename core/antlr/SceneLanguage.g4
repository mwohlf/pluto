grammar SceneLanguage;
// see: https://github.com/antlr/grammars-v4/blob/master/c/C.g4
// see: http://stackoverflow.com/questions/23098415/visitor-listener-code-for-a-while-loop-in-antlr-4

// --------  Parser Rules


parse
 : block EOF
 ;

block
 : (statement SColon 
     | functionDecl SColon)* 
   (Return expression SColon)?
 ;

statement
 : entity
 | assignment
 | functionCall
 | ifStatement
 | forStatement
 | whileStatement
 ;

assignment
 : Identifier indexes? Assign expression     #Assign
 | Identifier indexes? AssignP expression    #AssignPlus
 | Identifier indexes? AssignM expression    #AssignMinus
 ;

functionCall
 : Identifier OParen exprList? CParen  #identifierFunctionCall
 | Println OParen expression? CParen   #printlnFunctionCall
 | Print OParen expression CParen      #printFunctionCall
 | Assert OParen expression CParen     #assertFunctionCall
 | Size OParen expression CParen       #sizeFunctionCall
 ;

ifStatement
 : ifStat elseIfStat* elseStat? End
 ;

ifStat
 : If expression Do block
 ;

elseIfStat
 : Else If expression Do block
 ;

elseStat
 : Else Do block
 ;

functionDecl
 : Def Identifier OParen idList? CParen block End
 ;

forStatement
 : For Identifier Assign expression To expression Do block End
 ;

whileStatement
 : While expression OBrace block CBrace
 ;

idList
 : Identifier (Comma Identifier)*
 ;

exprList
 : expression (Comma expression)*
 ;

expression
 : Minus expression                         #unaryMinusExpression
 | Excl expression                          #notExpression
 | expression Pow expression                #powerExpression
 | expression Multiply expression           #multiplyExpression
 | expression Divide expression             #divideExpression
 | expression Modulus expression            #modulusExpression
 | expression Plus expression               #addExpression
 | expression Minus expression              #subtractExpression
 | expression GTEquals expression           #gtEqExpression
 | expression LTEquals expression           #ltEqExpression
 | expression GreaterThan expression        #gtExpression
 | expression LessThan expression           #ltExpression
 | expression Equals expression             #eqExpression
 | expression NEquals expression            #notEqExpression
 | expression AndAnd expression             #andAndExpression
 | expression OrOr expression               #orOrExpression
 | expression And expression                #andExpression
 | expression Or expression                 #orExpression
 | expression 
     QMark expression 
     Colon expression                       #ternaryExpression
 | expression In expression                 #inExpression
 | Number                                   #numberExpression
 | Bool                                     #boolExpression
 | Null                                     #nullExpression
 | functionCall indexes?                    #functionCallExpression
 | list indexes?                            #listExpression
 | Identifier indexes?                      #identifierExpression
 | Identifier PlusPlus                      #postIncrement
 | String indexes?                          #stringExpression
 | OParen expression CParen indexes?        #expressionExpression
 | Input OParen String? CParen              #inputExpression
 | entity                                   #entityDefinition
 | behavior                                 #behaviorDefinition
 | attribute                                #attributeDefinition
 | Constant                                 #constantExpression
 ;          
      
    
entity
    : entityType OBrace 
        (parameter ( Comma parameter)*)? Comma?
      CBrace;
    
behavior
    : behaviorType OBrace 
        (parameter (Comma parameter)*)? Comma?
      CBrace;    
    
attribute
    : attributeType OBrace 
        (parameter (Comma parameter)*)? Comma?
      CBrace;       
  
parameter
    : Identifier Colon expression
    | expression;
    
behaviorType
      : 'Spin'
      | 'Move'
      | 'MoveTo'
      | 'Seek'
      | 'Flee'
      | 'Parallel'
      | 'Sequential'     
      | 'FireLaser'
      | 'Delay'  
      | 'Align'     
      ;  
        
entityType
      : 'Cam'
      | 'Skybox'
      | 'Light'
      | 'Icosphere'
      | 'Box'
      | 'Object'
      | 'Waypoint'
      | 'Triangle'
      | 'Quad'
      | 'Planet'
      | 'Smoke'
      ;
      
attributeType
      : 'Position'
      | 'Color'   
      | 'Rotation'
      | 'Vector'
      ;
      
list
 : OBracket exprList? CBracket
 ;

indexes
 : (OBracket expression CBracket)+
 ;

Println  : 'println';
Print    : 'print';
Input    : 'input';
Assert   : 'assert';
Size     : 'size';
Def      : 'def';
If       : 'if';
Else     : 'else';
Return   : 'return';
For      : 'for';
While    : 'while';
To       : 'to';
Do       : 'do';
End      : 'end';
In       : 'in';
Null     : 'null';

OrOr        : '||';
AndAnd      : '&&';
Equals      : '==';
NEquals     : '!=';
GTEquals    : '>=';
LTEquals    : '<=';
Pow         : '^';
Excl        : '!';
GreaterThan : '>';
LessThan    : '<';
Plus        : '+';
PlusPlus    : '++';
Minus       : '-';
Multiply    : '*';
Divide      : '/';
Modulus     : '%';
OBrace      : '{';
CBrace      : '}';
OBracket    : '[';
CBracket    : ']';
OParen      : '(';
CParen      : ')';
SColon      : ';';
Assign      : '=';
AssignM     : '=-';
AssignP     : '=+';
Comma       : ',';
QMark       : '?';
Colon       : ':';
Or          : '|';
And         : '&';

Bool
 : 'true' 
 | 'false'
 ;


Number
 : [+|-]? Digit+ ('.' Digit*)?
 ;

Identifier
 : [a-z] [a-zA-Z_0-9]*
 ;

Constant
 : [A-Z] [A-Z_0-9]*
 ;

String
 : ["] (~["\r\n] | '\\\\' | '\\"')* ["]
 | ['] (~['\r\n] | '\\\\' | '\\\'')* [']
 ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '#' ~[\r\n]*) -> skip
 ;

Space
 : [ \t\r\n\u000C] -> skip
 ;

fragment Digit 
 : [0-9]
 ;
 
