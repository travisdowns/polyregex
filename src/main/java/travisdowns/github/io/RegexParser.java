// Output created by jacc on Fri Apr 19 20:10:07 COT 2019

package travisdowns.github.io;

class RegexParser extends ParserBase implements RegexTokens {
    private int yyss = 100;
    private int yytok;
    private int yysp = 0;
    private int[] yyst;
    protected int yyerrno = (-1);
    private Object[] yysv;
    private Object yyrv;

    public boolean parse() {
        int yyn = 0;
        yysp = 0;
        yyst = new int[yyss];
        yysv = new Object[yyss];
        yytok = (lexer.getToken()
                 );
    loop:
        for (;;) {
            switch (yyn) {
                case 0:
                    yyst[yysp] = 0;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 25:
                    switch (yytok) {
                        case CHAR:
                            yyn = 6;
                            continue;
                        case '(':
                            yyn = 7;
                            continue;
                        case '.':
                            yyn = 8;
                            continue;
                        case ENDINPUT:
                            yyn = yyr1();
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 1:
                    yyst[yysp] = 1;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 26:
                    switch (yytok) {
                        case ENDINPUT:
                            yyn = 50;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 2:
                    yyst[yysp] = 2;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 27:
                    yyn = yys2();
                    continue;

                case 3:
                    yyst[yysp] = 3;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 28:
                    switch (yytok) {
                        case '|':
                            yyn = 10;
                            continue;
                        case ENDINPUT:
                            yyn = yyr2();
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 4:
                    yyst[yysp] = 4;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 29:
                    switch (yytok) {
                        case '?':
                        case ':':
                        case error:
                        case '*':
                        case '+':
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr5();
                    continue;

                case 5:
                    yyst[yysp] = 5;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 30:
                    switch (yytok) {
                        case error:
                        case ':':
                            yyn = 53;
                            continue;
                        case '*':
                            yyn = 11;
                            continue;
                        case '+':
                            yyn = 12;
                            continue;
                        case '?':
                            yyn = 13;
                            continue;
                    }
                    yyn = yyr7();
                    continue;

                case 6:
                    yyst[yysp] = 6;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 31:
                    switch (yytok) {
                        case ':':
                        case error:
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr17();
                    continue;

                case 7:
                    yyst[yysp] = 7;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 32:
                    switch (yytok) {
                        case '?':
                            yyn = 15;
                            continue;
                        case '.':
                        case '(':
                        case CHAR:
                            yyn = yyr14();
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 8:
                    yyst[yysp] = 8;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 33:
                    switch (yytok) {
                        case ':':
                        case error:
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr18();
                    continue;

                case 9:
                    yyst[yysp] = 9;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 34:
                    switch (yytok) {
                        case '?':
                        case ':':
                        case error:
                        case '*':
                        case '+':
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr6();
                    continue;

                case 10:
                    yyst[yysp] = 10;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 35:
                    switch (yytok) {
                        case CHAR:
                            yyn = 6;
                            continue;
                        case '(':
                            yyn = 7;
                            continue;
                        case '.':
                            yyn = 8;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 11:
                    yyst[yysp] = 11;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 36:
                    switch (yytok) {
                        case '+':
                        case ':':
                        case error:
                        case '*':
                            yyn = 53;
                            continue;
                        case '?':
                            yyn = 17;
                            continue;
                    }
                    yyn = yyr8();
                    continue;

                case 12:
                    yyst[yysp] = 12;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 37:
                    switch (yytok) {
                        case '+':
                        case ':':
                        case error:
                        case '*':
                            yyn = 53;
                            continue;
                        case '?':
                            yyn = 18;
                            continue;
                    }
                    yyn = yyr10();
                    continue;

                case 13:
                    yyst[yysp] = 13;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 38:
                    switch (yytok) {
                        case '+':
                        case ':':
                        case error:
                        case '*':
                            yyn = 53;
                            continue;
                        case '?':
                            yyn = 19;
                            continue;
                    }
                    yyn = yyr12();
                    continue;

                case 14:
                    yyst[yysp] = 14;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 39:
                    switch (yytok) {
                        case CHAR:
                            yyn = 6;
                            continue;
                        case '(':
                            yyn = 7;
                            continue;
                        case '.':
                            yyn = 8;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 15:
                    yyst[yysp] = 15;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 40:
                    switch (yytok) {
                        case ':':
                            yyn = 21;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 16:
                    yyst[yysp] = 16;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 41:
                    yyn = yys16();
                    continue;

                case 17:
                    yyst[yysp] = 17;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 42:
                    switch (yytok) {
                        case '?':
                        case ':':
                        case error:
                        case '*':
                        case '+':
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr9();
                    continue;

                case 18:
                    yyst[yysp] = 18;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 43:
                    switch (yytok) {
                        case '?':
                        case ':':
                        case error:
                        case '*':
                        case '+':
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr11();
                    continue;

                case 19:
                    yyst[yysp] = 19;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 44:
                    switch (yytok) {
                        case '?':
                        case ':':
                        case error:
                        case '*':
                        case '+':
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr13();
                    continue;

                case 20:
                    yyst[yysp] = 20;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 45:
                    switch (yytok) {
                        case '|':
                            yyn = 10;
                            continue;
                        case ')':
                            yyn = 22;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 21:
                    yyst[yysp] = 21;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 46:
                    switch (yytok) {
                        case CHAR:
                            yyn = 6;
                            continue;
                        case '(':
                            yyn = 7;
                            continue;
                        case '.':
                            yyn = 8;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 22:
                    yyst[yysp] = 22;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 47:
                    switch (yytok) {
                        case ':':
                        case error:
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr15();
                    continue;

                case 23:
                    yyst[yysp] = 23;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 48:
                    switch (yytok) {
                        case '|':
                            yyn = 10;
                            continue;
                        case ')':
                            yyn = 24;
                            continue;
                    }
                    yyn = 53;
                    continue;

                case 24:
                    yyst[yysp] = 24;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 49:
                    switch (yytok) {
                        case ':':
                        case error:
                            yyn = 53;
                            continue;
                    }
                    yyn = yyr16();
                    continue;

                case 50:
                    return true;
                case 51:
                    yyerror("stack overflow");
                case 52:
                    return false;
                case 53:
                    yyerror("syntax error");
                    return false;
            }
        }
    }

    protected void yyexpand() {
        int[] newyyst = new int[2*yyst.length];
        Object[] newyysv = new Object[2*yyst.length];
        for (int i=0; i<yyst.length; i++) {
            newyyst[i] = yyst[i];
            newyysv[i] = yysv[i];
        }
        yyst = newyyst;
        yysv = newyysv;
    }

    private int yys2() {
        switch (yytok) {
            case CHAR:
                return 6;
            case '(':
                return 7;
            case '.':
                return 8;
            case ENDINPUT:
            case '|':
            case ')':
                return yyr3();
        }
        return 53;
    }

    private int yys16() {
        switch (yytok) {
            case CHAR:
                return 6;
            case '(':
                return 7;
            case '.':
                return 8;
            case ENDINPUT:
            case '|':
            case ')':
                return yyr4();
        }
        return 53;
    }

    private int yyr1() { // line : /* empty */
        {
        start = State.MATCHSTATE;
    }
        yysv[yysp-=0] = yyrv;
        return 1;
    }

    private int yyr2() { // line : alt
        {
        Frag f = paren(((Frag)yysv[yysp-1]), 0);
        patch(f.out, State.MATCHSTATE);
        start = f.start;
    }
        yysv[yysp-=1] = yyrv;
        return 1;
    }

    private int yyr5() { // concat : repeat
        yysp -= 1;
        return yypconcat();
    }

    private int yyr6() { // concat : concat repeat
        {
        patch(((Frag)yysv[yysp-2]).out, ((Frag)yysv[yysp-1]).start);
        yyrv = new Frag(((Frag)yysv[yysp-2]).start, ((Frag)yysv[yysp-1]).out);
    }
        yysv[yysp-=2] = yyrv;
        return yypconcat();
    }

    private int yypconcat() {
        switch (yyst[yysp-1]) {
            case 10: return 16;
            default: return 2;
        }
    }

    private int yyr14() { // count : /* empty */
        { yyrv = ++nparen; }
        yysv[yysp-=0] = yyrv;
        return 14;
    }

    private int yyr3() { // alt : concat
        yysp -= 1;
        return yypalt();
    }

    private int yyr4() { // alt : alt '|' concat
        {
        State s = State.makeSplit(id++, ((Frag)yysv[yysp-3]).start, ((Frag)yysv[yysp-1]).start);
        yyrv = new Frag(s, append(((Frag)yysv[yysp-3]).out, ((Frag)yysv[yysp-1]).out));
    }
        yysv[yysp-=3] = yyrv;
        return yypalt();
    }

    private int yypalt() {
        switch (yyst[yysp-1]) {
            case 14: return 20;
            case 0: return 3;
            default: return 23;
        }
    }

    private int yyr7() { // repeat : single
        yysp -= 1;
        return yyprepeat();
    }

    private int yyr8() { // repeat : single '*'
        {
        State s = State.makeSplit(id++, ((Frag)yysv[yysp-2]).start, null);
        patch(((Frag)yysv[yysp-2]).out, s);
        yyrv = new Frag(s, s.out1);
    }
        yysv[yysp-=2] = yyrv;
        return yyprepeat();
    }

    private int yyr9() { // repeat : single '*' '?'
        {
        State s = State.makeSplit(id++, null, ((Frag)yysv[yysp-3]).start);
        patch(((Frag)yysv[yysp-3]).out, s);
        yyrv = new Frag(s, s.out1);
    }
        yysv[yysp-=3] = yyrv;
        return yyprepeat();
    }

    private int yyr10() { // repeat : single '+'
        {
        State s = State.makeSplit(id++, ((Frag)yysv[yysp-2]).start, null);
        patch(((Frag)yysv[yysp-2]).out, s);
        yyrv = new Frag(((Frag)yysv[yysp-2]).start, s.out1);
    }
        yysv[yysp-=2] = yyrv;
        return yyprepeat();
    }

    private int yyr11() { // repeat : single '+' '?'
        {
        State s = State.makeSplit(id++, null, ((Frag)yysv[yysp-3]).start);
        patch(((Frag)yysv[yysp-3]).out, s);
        yyrv = new Frag(((Frag)yysv[yysp-3]).start, s.out);
    }
        yysv[yysp-=3] = yyrv;
        return yyprepeat();
    }

    private int yyr12() { // repeat : single '?'
        {
        State s = State.makeSplit(id++, ((Frag)yysv[yysp-2]).start, null);
        yyrv = new Frag(s, append(((Frag)yysv[yysp-2]).out, s.out1));
    }
        yysv[yysp-=2] = yyrv;
        return yyprepeat();
    }

    private int yyr13() { // repeat : single '?' '?'
        {
        State s = State.makeSplit(id++, null, ((Frag)yysv[yysp-3]).start);
        yyrv = new Frag(s, append(((Frag)yysv[yysp-3]).out, s.out));
    }
        yysv[yysp-=3] = yyrv;
        return yyprepeat();
    }

    private int yyprepeat() {
        switch (yyst[yysp-1]) {
            case 16: return 9;
            case 2: return 9;
            default: return 4;
        }
    }

    private int yyr15() { // single : '(' count alt ')'
        {
        yyrv = paren(((Frag)yysv[yysp-2]), ((Integer)yysv[yysp-3]));
    }
        yysv[yysp-=4] = yyrv;
        return 5;
    }

    private int yyr16() { // single : '(' '?' ':' alt ')'
        {
        yyrv = ((Frag)yysv[yysp-2]);
    }
        yysv[yysp-=5] = yyrv;
        return 5;
    }

    private int yyr17() { // single : CHAR
        {
        State s = State.makeChar(id++, ((Character)yysv[yysp-1]));
        yyrv = new Frag(s, s.out);
    }
        yysv[yysp-=1] = yyrv;
        return 5;
    }

    private int yyr18() { // single : '.'
        {
        State s = State.makeDot(id++);
        yyrv = new Frag(s, s.out);
    }
        yysv[yysp-=1] = yyrv;
        return 5;
    }

    protected String[] yyerrmsgs = {
    };


    RegexParser(String input) { super(input); }

}
