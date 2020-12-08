grammar SCIMFilter;

parse
:
	filter
;

filter
:
    attrExp #attrExpression
    | valuePath #valuePathExpression
    | filter SP AND SP filter #andExpression
    | filter SP OR SP filter #orExpression
    | (NOT SP)? '(' filter ')' #braceExpression

;

attrExp:
    attrPath SP PRESENT #presentExpression
    | attrPath SP OPERATOR SP PARAM #logExpression
;

valuePath:
    attrPath SP* LEFT_BRACKET SP* (valFilter | valExpression) SP* RIGHT_BRACKET SUBATTRNAME?
;

valExpression:
  pagingQuery
  | valFilter SP* '&' SP* pagingQuery
  | pagingQuery SP* '&' SP* valFilter

;

pagingQuery:
    PAGING_ASSIGNMENT ('&'PAGING_ASSIGNMENT)*?
;

PAGING_ASSIGNMENT:
    PAGING_PARAMS '=' UNRESERVED+
;

valFilter:
      attrExp #valAttrExpression
    | valuePath #valValuePathExpression
    | valFilter SP AND SP valFilter #valAndExpression
    | valFilter SP OR SP valFilter #valOrExpression
    | (NOT SP)? '(' valFilter ')' #valBraceExpression
;

attrPath:
    URI? ATTRNAME SUBATTRNAME?
;

subAttrPath:
    SUBATTRNAME
;

fragment UNRESERVED:
    ALPHA
    | DIGIT
    | '-'
    | '.'
    | '_'
    | '~'
;

fragment RESERVED:
    GEN_DELIMITERS
    | SUB_DELIMITERS
;

fragment GEN_DELIMITERS:
    ':'
    | '|'
    | '?'
    | '#'
    | '['
    | ']'
    | '@'
;

fragment SUB_DELIMITERS:
    '!'
    | '$'
    | '&'
    | '\''
    | '('
    | ')'
    | '*'
    | '+'
    | ','
    | ';'
    | '='
;

PAGING_PARAMS:
    'startIndex'
    | 'startId'
    | 'count'
;

OR:
	[oO] [rR]
;

AND:
	[aA] [nN] [dD]
;

NOT:
	[nN] [oO] [tT]
;

SP:
    ' '
;

PRESENT:
	[pP] [rR]
;

OPERATOR:
	  [eE] [qQ]
	| [eE] [wW]
	| [sS] [wW]
	| [cC] [oO]
	| [gG] [tT]
	| [gG] [eE]
	| [lL] [tT]
	| [lL] [eE]
	| [nN] [eE]
;

LEFT_BRACKET:
    '['
;

RIGHT_BRACKET:
    ']'
;

PARAM:
    STRING | INTEGER | DECIMAL | BOOLEAN
;

INTEGER:
    [+-]? DIGIT+
;

DECIMAL:
    [+-]? DIGIT* '.' DIGIT+ ( [eE] [+-]? DIGIT+ )?
;

BOOLEAN:
      [tT] [rR] [uU] [eE]
	| [fF] [aA] [lL] [sS] [eE]
;

fragment ALPHA:
    [a-z]
	| [A-Z]
;

fragment DIGIT:
    [0-9]
;

ATTRNAME:
    ALPHA ('-' | '_' | DIGIT | ALPHA)*
;

SUBATTRNAME:
    '.' ATTRNAME
;

URI:

    ALPHA (ALPHA | DIGIT | '+' | '-' | '.')* ':' (ALPHA | DIGIT | '-' | '.' | '_' | '~' | ':' | '@')+ ':'
;

fragment ESCAPED_QUOTE:
	'\\"'
;

STRING:
    '"' (ESC | SAFECODEPOINT)* '"'
;

fragment ESC:
    '\\' (["\\/bfnrt] | UNICODE)
;
fragment UNICODE:
    'u' HEX HEX HEX HEX
;
fragment HEX:
    [0-9a-fA-F]
;
fragment SAFECODEPOINT:
    ~ ["\\\u0000-\u001F]
;

WS:
	[ \t\r\n] + -> skip
; //skipping backspaces, tabs, carriage returns, newlines and spaces

