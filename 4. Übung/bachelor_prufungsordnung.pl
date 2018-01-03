offen(clara, tgi, ev).

studienzeit_nicht_ueberschritten(clara).

angemeldet(clara,tgi).
angemeldet(clara,ra).

allg_zugelassen(clara).

abgelegt(tgi, clara, 4.0).
abgelegt(ra, clara, 4.0).
abgelegt(mathe, clara, 3.9).

bachelorarbeit(clara, 4.0).
kolloquium(clara, 4.0).




alle_module_bestanden(X,[Head|Tail]) :-
	(Head == ende);
	(abgelegt(Head, X, Note),
	Note =< 4.0,
	alle_module_bestanden(X,Tail)).

verbesserungsversuch(X,Y) :- 
	wurde_verbessert(X,Y,nein).

allg_zugelassen(X) :-
	studienzeit_nicht_ueberschritten(X),
	eingeschrieben(X, bachelor_informatik, tu_dresden),
	erklaerung(X).

pruefungszulassung(X,Y) :-
	allg_zugelassen(X),
	angemeldet(X,Y),
	offen(X,Y,_).
	/*(freiversuch(X,Y);
	erstpruefung(X,Y);
	erste_wiederholung(X,Y);
	zweite_wiederholung(X,Y);
	verbesserungsversuch(X,Y)).*/

zulassung_bachelorarbeit(X) :-
	allg_zugelassen(X),
	antrag_ausgabe_bachelorthema(X),
	(b_erstpruefung(X);
	b_erste_wiederholung(X)).

zulassung_kolloquium(X) :-
	allg_zugelassen(X),
	bachelorarbeit(X, Note),
	Note =< 4.0.

abschluss_erhalten(X) :-
	bachelorarbeit(X, Bnote),
	Bnote =< 4.0,
	kolloquium(X, Knote),
	Knote =< 4.0,
	alle_module_bestanden(X, [tgi,ra,mathe, ende]).


	