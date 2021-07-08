# PartialMonitor

Very small Java program to transform LamaConv monitors into partial monitors.

## How to install

- Install LamaConv (https://www.isp.uni-luebeck.de/lamaconv)

## How to use

Compile the Java code.

```bash
-$ javac Monitor.java
```

Execute the program

```bash
-$ java Monitor <path_to_lamaconv> <LTL_property> <alphabet>
```

where <path_to_lamaconv> is the path to the folder where LamaConv has been installed (where rltlconv.jar is), <LTL_property> is the LTL property to use for synthesising the monitor, and <alphabet> is the list of alphabet of the system under analysis.

For instance

```bash
-$ java Monitor <path_to_lamaconv> "((p && Fq) || (s && GFr))" [s,p,q,r]
```

will generate

```bash
MOORE {
	ALPHABET = [s,  p,  q,  r]
	STATES = [eeq0:false, q1q1:?, q0q2:?, q0q0:x, q0ee:true]
	START = q1q1
	DELTA(eeq0, ?) = eeq0
	DELTA(q1q1, p) = q0q2
	DELTA(q1q1, q) = eeq0
	DELTA(q1q1, r) = eeq0
	DELTA(q1q1, s) = q0q0
	DELTA(q0q2, p) = q0q2
	DELTA(q0q2, q) = q0ee
	DELTA(q0q2, r) = q0q2
	DELTA(q0q2, s) = q0q2
	DELTA(q0q0, ?) = q0q0
	DELTA(q0ee, ?) = q0ee
}
```

a monitor with the additional (x) state representing when the monitor should give up on verifying the property. Indeed, if the monitor observes s, then there is nothing else to check, since GFr will never be neither satisfied nor violated.
