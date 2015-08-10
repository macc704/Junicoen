// Generated from ExtendedExpression.g4 by ANTLR 4.5

	package net.unicoen.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExtendedExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LEFTPAREN=1, RIGHTPAREN=2, ZERO=3, NONZERODIGIT=4, ADD=5, SUB=6, INC=7, 
		DEC=8, MUL=9, DIV=10, DOT=11, LARGER=12, LARGEROREQUALS=13, EQUALS=14, 
		SMALLEROREQUALS=15, SMALLER=16, IF=17, ELSE=18, LEFTBRACE=19, RIGHTBRACE=20, 
		ID=21, SEMICOLON=22, CLASS=23, VOID=24, EQUAL=25, INT=26, DOUBLE=27, PUBLIC=28, 
		PRIVATE=29, ABSTRACT=30, PROTECTED=31, PACKAGE=32, STATIC=33, FINAL=34, 
		COMMA=35, WHILE=36, NEW=37, WS=38, TURTLE=39;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"LEFTPAREN", "RIGHTPAREN", "ZERO", "NONZERODIGIT", "ADD", "SUB", "INC", 
		"DEC", "MUL", "DIV", "DOT", "LARGER", "LARGEROREQUALS", "EQUALS", "SMALLEROREQUALS", 
		"SMALLER", "IF", "ELSE", "LEFTBRACE", "RIGHTBRACE", "ID", "SEMICOLON", 
		"CLASS", "VOID", "EQUAL", "INT", "DOUBLE", "PUBLIC", "PRIVATE", "ABSTRACT", 
		"PROTECTED", "PACKAGE", "STATIC", "FINAL", "COMMA", "WHILE", "NEW", "WS", 
		"TURTLE"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'0'", null, "'+'", "'-'", "'++'", "'--'", "'*'", 
		"'/'", "'.'", "'>'", "'>='", "'=='", "'<='", "'<'", "'if'", "'else'", 
		"'{'", "'}'", null, "';'", "'class'", "'void'", "'='", "'int'", "'double'", 
		"'public'", "'private'", "'abstract'", "'protected'", "'package'", "'static'", 
		"'final'", "','", "'while'", "'new'", null, "'Turtle'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LEFTPAREN", "RIGHTPAREN", "ZERO", "NONZERODIGIT", "ADD", "SUB", 
		"INC", "DEC", "MUL", "DIV", "DOT", "LARGER", "LARGEROREQUALS", "EQUALS", 
		"SMALLEROREQUALS", "SMALLER", "IF", "ELSE", "LEFTBRACE", "RIGHTBRACE", 
		"ID", "SEMICOLON", "CLASS", "VOID", "EQUAL", "INT", "DOUBLE", "PUBLIC", 
		"PRIVATE", "ABSTRACT", "PROTECTED", "PACKAGE", "STATIC", "FINAL", "COMMA", 
		"WHILE", "NEW", "WS", "TURTLE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public ExtendedExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ExtendedExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2)\u00ef\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13"+
		"\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21"+
		"\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26"+
		"\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\32"+
		"\3\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\37"+
		"\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \3 \3 \3 \3"+
		" \3!\3!\3!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3"+
		"#\3$\3$\3%\3%\3%\3%\3%\3%\3&\3&\3&\3&\3\'\6\'\u00e3\n\'\r\'\16\'\u00e4"+
		"\3\'\3\'\3(\3(\3(\3(\3(\3(\3(\2\2)\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n"+
		"\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30"+
		"/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)\3\2\4\4\2C"+
		"\\c|\5\2\13\f\16\17\"\"\u00ef\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2"+
		"\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2"+
		"+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2"+
		"\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2"+
		"C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3"+
		"\2\2\2\3Q\3\2\2\2\5S\3\2\2\2\7U\3\2\2\2\tW\3\2\2\2\13Y\3\2\2\2\r[\3\2"+
		"\2\2\17]\3\2\2\2\21`\3\2\2\2\23c\3\2\2\2\25e\3\2\2\2\27g\3\2\2\2\31i\3"+
		"\2\2\2\33k\3\2\2\2\35n\3\2\2\2\37q\3\2\2\2!t\3\2\2\2#v\3\2\2\2%y\3\2\2"+
		"\2\'~\3\2\2\2)\u0080\3\2\2\2+\u0082\3\2\2\2-\u0084\3\2\2\2/\u0086\3\2"+
		"\2\2\61\u008c\3\2\2\2\63\u0091\3\2\2\2\65\u0093\3\2\2\2\67\u0097\3\2\2"+
		"\29\u009e\3\2\2\2;\u00a5\3\2\2\2=\u00ad\3\2\2\2?\u00b6\3\2\2\2A\u00c0"+
		"\3\2\2\2C\u00c8\3\2\2\2E\u00cf\3\2\2\2G\u00d5\3\2\2\2I\u00d7\3\2\2\2K"+
		"\u00dd\3\2\2\2M\u00e2\3\2\2\2O\u00e8\3\2\2\2QR\7*\2\2R\4\3\2\2\2ST\7+"+
		"\2\2T\6\3\2\2\2UV\7\62\2\2V\b\3\2\2\2WX\4\63;\2X\n\3\2\2\2YZ\7-\2\2Z\f"+
		"\3\2\2\2[\\\7/\2\2\\\16\3\2\2\2]^\7-\2\2^_\7-\2\2_\20\3\2\2\2`a\7/\2\2"+
		"ab\7/\2\2b\22\3\2\2\2cd\7,\2\2d\24\3\2\2\2ef\7\61\2\2f\26\3\2\2\2gh\7"+
		"\60\2\2h\30\3\2\2\2ij\7@\2\2j\32\3\2\2\2kl\7@\2\2lm\7?\2\2m\34\3\2\2\2"+
		"no\7?\2\2op\7?\2\2p\36\3\2\2\2qr\7>\2\2rs\7?\2\2s \3\2\2\2tu\7>\2\2u\""+
		"\3\2\2\2vw\7k\2\2wx\7h\2\2x$\3\2\2\2yz\7g\2\2z{\7n\2\2{|\7u\2\2|}\7g\2"+
		"\2}&\3\2\2\2~\177\7}\2\2\177(\3\2\2\2\u0080\u0081\7\177\2\2\u0081*\3\2"+
		"\2\2\u0082\u0083\t\2\2\2\u0083,\3\2\2\2\u0084\u0085\7=\2\2\u0085.\3\2"+
		"\2\2\u0086\u0087\7e\2\2\u0087\u0088\7n\2\2\u0088\u0089\7c\2\2\u0089\u008a"+
		"\7u\2\2\u008a\u008b\7u\2\2\u008b\60\3\2\2\2\u008c\u008d\7x\2\2\u008d\u008e"+
		"\7q\2\2\u008e\u008f\7k\2\2\u008f\u0090\7f\2\2\u0090\62\3\2\2\2\u0091\u0092"+
		"\7?\2\2\u0092\64\3\2\2\2\u0093\u0094\7k\2\2\u0094\u0095\7p\2\2\u0095\u0096"+
		"\7v\2\2\u0096\66\3\2\2\2\u0097\u0098\7f\2\2\u0098\u0099\7q\2\2\u0099\u009a"+
		"\7w\2\2\u009a\u009b\7d\2\2\u009b\u009c\7n\2\2\u009c\u009d\7g\2\2\u009d"+
		"8\3\2\2\2\u009e\u009f\7r\2\2\u009f\u00a0\7w\2\2\u00a0\u00a1\7d\2\2\u00a1"+
		"\u00a2\7n\2\2\u00a2\u00a3\7k\2\2\u00a3\u00a4\7e\2\2\u00a4:\3\2\2\2\u00a5"+
		"\u00a6\7r\2\2\u00a6\u00a7\7t\2\2\u00a7\u00a8\7k\2\2\u00a8\u00a9\7x\2\2"+
		"\u00a9\u00aa\7c\2\2\u00aa\u00ab\7v\2\2\u00ab\u00ac\7g\2\2\u00ac<\3\2\2"+
		"\2\u00ad\u00ae\7c\2\2\u00ae\u00af\7d\2\2\u00af\u00b0\7u\2\2\u00b0\u00b1"+
		"\7v\2\2\u00b1\u00b2\7t\2\2\u00b2\u00b3\7c\2\2\u00b3\u00b4\7e\2\2\u00b4"+
		"\u00b5\7v\2\2\u00b5>\3\2\2\2\u00b6\u00b7\7r\2\2\u00b7\u00b8\7t\2\2\u00b8"+
		"\u00b9\7q\2\2\u00b9\u00ba\7v\2\2\u00ba\u00bb\7g\2\2\u00bb\u00bc\7e\2\2"+
		"\u00bc\u00bd\7v\2\2\u00bd\u00be\7g\2\2\u00be\u00bf\7f\2\2\u00bf@\3\2\2"+
		"\2\u00c0\u00c1\7r\2\2\u00c1\u00c2\7c\2\2\u00c2\u00c3\7e\2\2\u00c3\u00c4"+
		"\7m\2\2\u00c4\u00c5\7c\2\2\u00c5\u00c6\7i\2\2\u00c6\u00c7\7g\2\2\u00c7"+
		"B\3\2\2\2\u00c8\u00c9\7u\2\2\u00c9\u00ca\7v\2\2\u00ca\u00cb\7c\2\2\u00cb"+
		"\u00cc\7v\2\2\u00cc\u00cd\7k\2\2\u00cd\u00ce\7e\2\2\u00ceD\3\2\2\2\u00cf"+
		"\u00d0\7h\2\2\u00d0\u00d1\7k\2\2\u00d1\u00d2\7p\2\2\u00d2\u00d3\7c\2\2"+
		"\u00d3\u00d4\7n\2\2\u00d4F\3\2\2\2\u00d5\u00d6\7.\2\2\u00d6H\3\2\2\2\u00d7"+
		"\u00d8\7y\2\2\u00d8\u00d9\7j\2\2\u00d9\u00da\7k\2\2\u00da\u00db\7n\2\2"+
		"\u00db\u00dc\7g\2\2\u00dcJ\3\2\2\2\u00dd\u00de\7p\2\2\u00de\u00df\7g\2"+
		"\2\u00df\u00e0\7y\2\2\u00e0L\3\2\2\2\u00e1\u00e3\t\3\2\2\u00e2\u00e1\3"+
		"\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5"+
		"\u00e6\3\2\2\2\u00e6\u00e7\b\'\2\2\u00e7N\3\2\2\2\u00e8\u00e9\7V\2\2\u00e9"+
		"\u00ea\7w\2\2\u00ea\u00eb\7t\2\2\u00eb\u00ec\7v\2\2\u00ec\u00ed\7n\2\2"+
		"\u00ed\u00ee\7g\2\2\u00eeP\3\2\2\2\4\2\u00e4\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}