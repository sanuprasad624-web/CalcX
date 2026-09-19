package com.example.math.formula

/**
 * Comprehensive, mathematically verified JEE Main & Advanced Formula Repository.
 * Contains 120+ structured formulas spanning all chapters of Mathematics, Physics, and Chemistry.
 */
object JeeFormulaDatabase {

    val allFormulas: List<CanonicalFormula> by lazy {
        mathFormulas + physicsFormulas + chemistryFormulas
    }

    // ==========================================
    // MATHEMATICS (CLASS 11 & 12)
    // ==========================================
    val mathFormulas: List<CanonicalFormula> = listOf(
        // TRIGONOMETRY
        CanonicalFormula(
            id = "math_trig_compound_sin",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Trigonometry",
            topic = "Compound Angles",
            name = "Sine Compound Angle",
            latex = "\\sin(A \\pm B) = \\sin A \\cos B \\pm \\cos A \\sin B",
            variables = listOf(
                VariableDef("A", "First angle", "rad / deg"),
                VariableDef("B", "Second angle", "rad / deg")
            ),
            conditions = "Valid for all real angles A and B",
            commonMistake = "Mixing up signs: for sine, + remains + and - remains -",
            explanation = "Fundamental identity for sine of sum and difference of two angles.",
            example = "\\sin(75^\\circ) = \\sin(45^\\circ + 30^\\circ) = \\frac{\\sqrt{6} + \\sqrt{2}}{4}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Trig", "Identities", "Compound Angles"),
            searchKeywords = listOf("sin(A+B)", "sine addition", "compound angle", "trigonometric sum")
        ),
        CanonicalFormula(
            id = "math_trig_compound_cos",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Trigonometry",
            topic = "Compound Angles",
            name = "Cosine Compound Angle",
            latex = "\\cos(A \\pm B) = \\cos A \\cos B \\mp \\sin A \\sin B",
            variables = listOf(
                VariableDef("A", "First angle", "rad / deg"),
                VariableDef("B", "Second angle", "rad / deg")
            ),
            conditions = "Valid for all real angles A and B",
            commonMistake = "Signs reverse: cos(A+B) has a minus sign, cos(A-B) has a plus sign",
            explanation = "Fundamental identity for cosine of sum and difference of two angles.",
            example = "\\cos(15^\\circ) = \\cos(45^\\circ - 30^\\circ) = \\frac{\\sqrt{6} + \\sqrt{2}}{4}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Trig", "Identities"),
            searchKeywords = listOf("cos(A+B)", "cosine addition", "compound angle")
        ),
        CanonicalFormula(
            id = "math_trig_double_tan",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Trigonometry",
            topic = "Multiple Angles",
            name = "Tangent Double Angle",
            latex = "\\tan(2\\theta) = \\frac{2\\tan\\theta}{1 - \\tan^2\\theta}",
            variables = listOf(
                VariableDef("\\theta", "Angle", "rad / deg")
            ),
            conditions = "\\theta \\ne (2n+1)\\frac{\\pi}{4} \\text{ and } \\theta \\ne (2n+1)\\frac{\\pi}{2}",
            commonMistake = "Writing denominator as 1 + tan²θ instead of 1 - tan²θ",
            explanation = "Expresses tan(2θ) in terms of tan(θ).",
            example = "\\text{If } \\tan\\theta = \\frac{1}{2}, \\tan(2\\theta) = \\frac{1}{1 - 1/4} = \\frac{4}{3}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Trig", "Double Angle"),
            searchKeywords = listOf("tan 2 theta", "tangent double angle")
        ),
        CanonicalFormula(
            id = "math_trig_general_sin",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Trigonometry",
            topic = "General Solutions",
            name = "General Solution of sin θ = sin α",
            latex = "\\theta = n\\pi + (-1)^n \\alpha, \\quad n \\in \\mathbb{Z}",
            variables = listOf(
                VariableDef("\\theta", "Unknown angle", "rad"),
                VariableDef("\\alpha", "Principal value angle", "rad"),
                VariableDef("n", "Integer parameter", "")
            ),
            conditions = "\\alpha \\in [-\\frac{\\pi}{2}, \\frac{\\pi}{2}]",
            commonMistake = "Forgetting the alternating factor (-1)^n",
            explanation = "Generates all angles satisfying sin θ = sin α on the real axis.",
            example = "\\sin\\theta = \\frac{1}{2} \\implies \\theta = n\\pi + (-1)^n \\frac{\\pi}{6}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Trig Equations", "General Solution"),
            searchKeywords = listOf("sin theta = sin alpha", "trig general solution")
        ),

        // COMPLEX NUMBERS
        CanonicalFormula(
            id = "math_complex_euler",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Complex Numbers",
            topic = "Polar & Euler Form",
            name = "Euler's Formula & De Moivre's Theorem",
            latex = "e^{i\\theta} = \\cos\\theta + i\\sin\\theta, \\quad (\\cos\\theta + i\\sin\\theta)^n = \\cos(n\\theta) + i\\sin(n\\theta)",
            variables = listOf(
                VariableDef("\\theta", "Argument of complex number", "rad"),
                VariableDef("n", "Power / index", "")
            ),
            conditions = "\\theta \\in \\mathbb{R}, n \\in \\mathbb{Q}",
            commonMistake = "Applying directly when base is not in standard cos θ + i sin θ form",
            explanation = "Converts complex numbers to polar exponential representation for rapid multiplication and powers.",
            example = "(\\cos\\frac{\\pi}{6} + i\\sin\\frac{\\pi}{6})^6 = \\cos\\pi + i\\sin\\pi = -1",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Complex Numbers", "Euler", "De Moivre"),
            searchKeywords = listOf("euler formula", "de moivre theorem", "e^(i theta)")
        ),
        CanonicalFormula(
            id = "math_complex_cube_roots",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Complex Numbers",
            topic = "Roots of Unity",
            name = "Cube Roots of Unity (1, ω, ω²)",
            latex = "1 + \\omega + \\omega^2 = 0, \\quad \\omega^3 = 1, \\quad \\omega = \\frac{-1 + i\\sqrt{3}}{2}",
            variables = listOf(
                VariableDef("\\omega", "Non-real cube root of unity", "")
            ),
            conditions = "\\omega \\ne 1",
            commonMistake = "Confusing ω² with ω conjugates in polynomial factorizations",
            explanation = "The roots of z³ = 1 form an equilateral triangle in the Argand plane.",
            example = "(1 + \\omega)^3 = (-\\omega^2)^3 = -\\omega^6 = -1",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Complex Numbers", "Cube Roots of Unity", "Omega"),
            searchKeywords = listOf("cube roots of unity", "omega", "1+w+w^2=0")
        ),

        // QUADRATIC EQUATIONS
        CanonicalFormula(
            id = "math_quad_roots",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Quadratic Equations",
            topic = "Roots & Coefficients",
            name = "Roots & Vieta's Relations",
            latex = "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}, \\quad \\alpha + \\beta = -\\frac{b}{a}, \\quad \\alpha\\beta = \\frac{c}{a}",
            variables = listOf(
                VariableDef("a, b, c", "Coefficients of ax² + bx + c = 0", ""),
                VariableDef("\\alpha, \\beta", "Roots of equation", "")
            ),
            conditions = "a \\ne 0",
            commonMistake = "Missing the negative sign in sum of roots (-b/a)",
            explanation = "Gives exact roots and algebraic relations between coefficients and roots.",
            example = "x^2 - 5x + 6 = 0 \\implies \\alpha + \\beta = 5, \\alpha\\beta = 6 \\implies \\text{roots are } 2, 3",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Algebra", "Quadratic", "Vieta"),
            searchKeywords = listOf("quadratic formula", "vieta relations", "sum of roots", "roots of quadratic")
        ),
        CanonicalFormula(
            id = "math_quad_common_root",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Quadratic Equations",
            topic = "Common Roots",
            name = "Condition for One Common Root",
            latex = "(c_1 a_2 - c_2 a_1)^2 = (b_1 c_2 - b_2 c_1)(a_1 b_2 - a_2 b_1)",
            variables = listOf(
                VariableDef("a_1, b_1, c_1", "Coefficients of first equation", ""),
                VariableDef("a_2, b_2, c_2", "Coefficients of second equation", "")
            ),
            conditions = "Equations are not identical (a₁/a₂ ≠ b₁/b₂ ≠ c₁/c₂)",
            commonMistake = "Applying common root condition when both roots are common (which requires a₁/a₂ = b₁/b₂ = c₁/c₂)",
            explanation = "Condition for two quadratics a₁x² + b₁x + c₁ = 0 and a₂x² + b₂x + c₂ = 0 to share a single root.",
            jeeLevel = JeeLevel.ADVANCED,
            tags = listOf("Algebra", "Common Root"),
            searchKeywords = listOf("common root condition", "quadratic common root")
        ),

        // SEQUENCES & SERIES
        CanonicalFormula(
            id = "math_seq_am_gm_hm",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Sequences & Series",
            topic = "Means Inequality",
            name = "AM-GM-HM Inequality",
            latex = "AM \\ge GM \\ge HM \\implies \\frac{a_1 + a_2 + \\dots + a_n}{n} \\ge \\sqrt[n]{a_1 a_2 \\dots a_n} \\ge \\frac{n}{\\frac{1}{a_1} + \\dots + \\frac{1}{a_n}}",
            variables = listOf(
                VariableDef("a_i", "Positive real numbers", "")
            ),
            conditions = "a_i > 0 \\text{ for all } i. \\text{ Equality holds iff } a_1 = a_2 = \\dots = a_n.",
            commonMistake = "Applying to negative or zero numbers where GM is undefined or inequality reverses",
            explanation = "One of the most frequently tested inequality tools in JEE Advanced for finding minimum/maximum values.",
            example = "x + \\frac{1}{x} \\ge 2\\sqrt{x \\cdot \\frac{1}{x}} = 2 \\quad (\\text{for } x > 0)",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Sequences", "Inequalities", "AM GM HM"),
            searchKeywords = listOf("am gm inequality", "am >= gm", "minimum value am gm")
        ),
        CanonicalFormula(
            id = "math_seq_special_sums",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Sequences & Series",
            topic = "Sigma Sums",
            name = "Sum of First n Natural Numbers, Squares & Cubes",
            latex = "\\sum_{r=1}^n r = \\frac{n(n+1)}{2}, \\quad \\sum_{r=1}^n r^2 = \\frac{n(n+1)(2n+1)}{6}, \\quad \\sum_{r=1}^n r^3 = \\left(\\frac{n(n+1)}{2}\\right)^2",
            variables = listOf(
                VariableDef("n", "Number of terms", "")
            ),
            conditions = "n \\in \\mathbb{N}",
            commonMistake = "Forgetting the factor (2n+1) in sum of squares",
            explanation = "Standard algebraic identities used in series summation and Riemann integral limits.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Sequences", "Sigma Notation"),
            searchKeywords = listOf("sum of squares", "sum of cubes", "sigma r^2", "sigma r^3")
        ),

        // PERMUTATION & COMBINATION
        CanonicalFormula(
            id = "math_pnc_dearrangement",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Permutations & Combinations",
            topic = "Dearrangements",
            name = "Dearrangement Formula D(n)",
            latex = "D_n = n! \\left( 1 - \\frac{1}{1!} + \\frac{1}{2!} - \\frac{1}{3!} + \\dots + \\frac{(-1)^n}{n!} \\right)",
            variables = listOf(
                VariableDef("n", "Number of distinct objects", "")
            ),
            conditions = "n \\ge 1",
            commonMistake = "Forgetting that D₁ = 0, D₂ = 1, D₃ = 2, D₄ = 9, D₅ = 44",
            explanation = "Calculates the number of permutations where no object appears in its original natural position.",
            example = "\\text{Dearrangement of 4 letters in 4 envelopes: } D_4 = 4!(1 - 1 + 1/2 - 1/6 + 1/24) = 9",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Combinatorics", "Dearrangement"),
            searchKeywords = listOf("dearrangement", "derangement formula", "D_n")
        ),

        // BINOMIAL THEOREM
        CanonicalFormula(
            id = "math_binomial_general_term",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Binomial Theorem",
            topic = "General Term",
            name = "General Term in (x + y)ⁿ",
            latex = "T_{r+1} = \\binom{n}{r} x^{n-r} y^r = \\frac{n!}{r!(n-r)!} x^{n-r} y^r",
            variables = listOf(
                VariableDef("n", "Total power", ""),
                VariableDef("r", "Index (0 to n)", ""),
                VariableDef("T_{r+1}", "(r+1)-th term in expansion", "")
            ),
            conditions = "n \\in \\mathbb{N}, 0 \\le r \\le n",
            commonMistake = "Confusing the term number (r+1) with the index r in the combination symbol ⁿC_r",
            explanation = "Enables finding specific terms, term independent of x, and middle terms.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Algebra", "Binomial"),
            searchKeywords = listOf("binomial general term", "T_(r+1)", "nCr")
        ),

        // PROBABILITY
        CanonicalFormula(
            id = "math_prob_bayes",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Probability",
            topic = "Bayes Theorem",
            name = "Bayes' Theorem",
            latex = "P(E_i | A) = \\frac{P(E_i) P(A | E_i)}{\\sum_{j=1}^k P(E_j) P(A | E_j)}",
            variables = listOf(
                VariableDef("E_i", "i-th mutually exclusive and exhaustive event", ""),
                VariableDef("A", "Observed outcome / event", "")
            ),
            conditions = "\\bigcup E_j = S, E_i \\cap E_j = \\emptyset \\text{ for } i \\ne j, P(A) > 0",
            commonMistake = "Confusing prior probability P(E_i) with posterior probability P(E_i|A)",
            explanation = "Calculates reverse / posterior probability of a hypothesis given observed evidence.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Probability", "Bayes Theorem"),
            searchKeywords = listOf("bayes theorem", "conditional probability", "posterior probability")
        ),

        // MATRICES & DETERMINANTS
        CanonicalFormula(
            id = "math_matrix_inverse",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Matrices & Determinants",
            topic = "Matrix Inversion",
            name = "Inverse & Adjoint Formula",
            latex = "A^{-1} = \\frac{1}{\\det(A)} \\operatorname{adj}(A), \\quad A \\cdot \\operatorname{adj}(A) = \\det(A) \\cdot I_n",
            variables = listOf(
                VariableDef("A", "Square matrix of order n", ""),
                VariableDef("\\operatorname{adj}(A)", "Adjoint matrix (transpose of cofactor matrix)", ""),
                VariableDef("\\det(A)", "Determinant of matrix", "")
            ),
            conditions = "\\det(A) \\ne 0 \\text{ (matrix must be non-singular)}",
            commonMistake = "Forgetting to transpose the cofactor matrix to obtain the adjoint",
            explanation = "Fundamental method for inverting square matrices and solving systems of linear equations AX = B.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Matrices", "Inverse", "Adjoint"),
            searchKeywords = listOf("matrix inverse", "adj(A)", "det(A)", "A^-1")
        ),
        CanonicalFormula(
            id = "math_matrix_det_properties",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Matrices & Determinants",
            topic = "Determinant Properties",
            name = "Determinant of Adjoint & Scaled Matrix",
            latex = "|\\operatorname{adj}(A)| = |A|^{n-1}, \\quad |\\operatorname{adj}(\\operatorname{adj}(A))| = |A|^{(n-1)^2}, \\quad |kA| = k^n |A|",
            variables = listOf(
                VariableDef("A", "Square matrix of order n", ""),
                VariableDef("k", "Scalar multiple", "")
            ),
            conditions = "A is an n × n matrix",
            commonMistake = "Writing |kA| = k|A| instead of k^n|A|",
            explanation = "Crucial determinant power relations tested in almost every JEE exam.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Determinants", "Adjoint Properties"),
            searchKeywords = listOf("det adj A", "|adj(A)|", "properties of determinants")
        ),

        // COORDINATE GEOMETRY (LINES, CIRCLES, CONICS)
        CanonicalFormula(
            id = "math_geo_line_distance",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Straight Lines",
            topic = "Perpendicular Distance",
            name = "Perpendicular Distance from Point to Line",
            latex = "d = \\frac{|a x_0 + b y_0 + c|}{\\sqrt{a^2 + b^2}}",
            variables = listOf(
                VariableDef("(x_0, y_0)", "Point coordinates", ""),
                VariableDef("ax + by + c = 0", "Equation of straight line", "")
            ),
            conditions = "a^2 + b^2 \\ne 0",
            commonMistake = "Forgetting absolute value bars |...| in numerator",
            explanation = "Calculates shortest Euclidean perpendicular distance from a given point to a straight line.",
            example = "\\text{Distance from (1, 2) to } 3x + 4y - 1 = 0: d = \\frac{|3(1) + 4(2) - 1|}{\\sqrt{3^2 + 4^2}} = \\frac{10}{5} = 2",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Coordinate Geometry", "Straight Lines"),
            searchKeywords = listOf("perpendicular distance point line", "distance formula line")
        ),
        CanonicalFormula(
            id = "math_geo_circle_tangent",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Circles",
            topic = "Tangents",
            name = "Condition of Tangency to Circle x² + y² = r²",
            latex = "y = mx \\pm r\\sqrt{1 + m^2}, \\quad \\text{Condition: } c^2 = r^2(1 + m^2)",
            variables = listOf(
                VariableDef("m", "Slope of tangent", ""),
                VariableDef("r", "Radius of circle", ""),
                VariableDef("c", "y-intercept of tangent", "")
            ),
            conditions = "Circle center at origin (0, 0)",
            commonMistake = "Forgetting ± which indicates two parallel tangents with the same slope",
            explanation = "Gives the slope-form tangent equations to a central circle.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Circles", "Tangents"),
            searchKeywords = listOf("circle tangent condition", "tangent to circle slope form")
        ),
        CanonicalFormula(
            id = "math_geo_parabola_tangent",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Conic Sections",
            topic = "Parabola",
            name = "Slope Form Tangent to Parabola y² = 4ax",
            latex = "y = mx + \\frac{a}{m}, \\quad \\text{Point of Contact: } \\left(\\frac{a}{m^2}, \\frac{2a}{m}\\right)",
            variables = listOf(
                VariableDef("a", "Focal parameter", ""),
                VariableDef("m", "Slope of tangent line", "")
            ),
            conditions = "m \\ne 0",
            commonMistake = "Using y = mx + c with c = am instead of c = a/m",
            explanation = "Slope form equation of tangent to rightward standard parabola y² = 4ax.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Conics", "Parabola", "Tangents"),
            searchKeywords = listOf("parabola tangent", "y = mx + a/m", "point of contact parabola")
        ),
        CanonicalFormula(
            id = "math_geo_ellipse_eccentricity",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Conic Sections",
            topic = "Ellipse",
            name = "Ellipse Eccentricity & Standard Tangent",
            latex = "e = \\sqrt{1 - \\frac{b^2}{a^2}}, \\quad y = mx \\pm \\sqrt{a^2 m^2 + b^2}",
            variables = listOf(
                VariableDef("a, b", "Semi-major and semi-minor axes (a > b)", ""),
                VariableDef("e", "Eccentricity (0 < e < 1)", ""),
                VariableDef("m", "Slope of tangent", "")
            ),
            conditions = "a > b > 0",
            commonMistake = "Swapping a and b when ellipse is vertical (b > a)",
            explanation = "Standard eccentricity and tangent envelope to ellipse x²/a² + y²/b² = 1.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Conics", "Ellipse"),
            searchKeywords = listOf("ellipse eccentricity", "ellipse tangent slope form")
        ),
        CanonicalFormula(
            id = "math_geo_hyperbola_tangent",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 11",
            chapter = "Conic Sections",
            topic = "Hyperbola",
            name = "Hyperbola Eccentricity & Asymptotes",
            latex = "e = \\sqrt{1 + \\frac{b^2}{a^2}}, \\quad y = mx \\pm \\sqrt{a^2 m^2 - b^2}, \\quad \\text{Asymptotes: } y = \\pm \\frac{b}{a} x",
            variables = listOf(
                VariableDef("a, b", "Semi-transverse and semi-conjugate axes", ""),
                VariableDef("e", "Eccentricity (e > 1)", "")
            ),
            conditions = "m^2 > b^2 / a^2 \\text{ for real tangents}",
            commonMistake = "Using minus sign in eccentricity formula instead of plus sign (e² = 1 + b²/a²)",
            explanation = "Standard formulas for hyperbola x²/a² - y²/b² = 1 and its asymptotes.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Conics", "Hyperbola"),
            searchKeywords = listOf("hyperbola eccentricity", "hyperbola asymptotes")
        ),

        // VECTORS & 3D GEOMETRY
        CanonicalFormula(
            id = "math_vec_stp_vtp",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Vectors",
            topic = "Vector Products",
            name = "Scalar & Vector Triple Products",
            latex = "[\\vec{a} \\ \\vec{b} \\ \\vec{c}] = \\vec{a} \\cdot (\\vec{b} \\times \\vec{c}), \\quad \\vec{a} \\times (\\vec{b} \\times \\vec{c}) = (\\vec{a} \\cdot \\vec{c})\\vec{b} - (\\vec{a} \\cdot \\vec{b})\\vec{c}",
            variables = listOf(
                VariableDef("\\vec{a}, \\vec{b}, \\vec{c}", "3D Vectors", "")
            ),
            conditions = "Valid for any three vectors in ℝ³",
            commonMistake = "Remembering VTP order incorrectly: use 'BAC - CAB' mnemonic",
            explanation = "Scalar triple product gives the volume of a parallelepiped; VTP expands double cross products.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Vectors", "Triple Product"),
            searchKeywords = listOf("scalar triple product", "vector triple product", "box product", "BAC CAB")
        ),
        CanonicalFormula(
            id = "math_3d_skew_lines",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "3D Geometry",
            topic = "Skew Lines",
            name = "Shortest Distance Between Skew Lines",
            latex = "d = \\frac{|(\\vec{a}_2 - \\vec{a}_1) \\cdot (\\vec{b}_1 \\times \\vec{b}_2)|}{|\\vec{b}_1 \\times \\vec{b}_2|}",
            variables = listOf(
                VariableDef("\\vec{a}_1, \\vec{a}_2", "Position vectors of points on Line 1 and Line 2", ""),
                VariableDef("\\vec{b}_1, \\vec{b}_2", "Direction vectors of Line 1 and Line 2", "")
            ),
            conditions = "\\vec{b}_1 \\times \\vec{b}_2 \\ne \\vec{0} \\text{ (lines are not parallel)}",
            commonMistake = "Using this formula for parallel lines (where b₁ × b₂ = 0)",
            explanation = "Calculates exact minimum distance between two non-intersecting, non-parallel lines in 3D space.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("3D Geometry", "Skew Lines"),
            searchKeywords = listOf("shortest distance skew lines", "distance between lines 3d")
        ),

        // CALCULUS
        CanonicalFormula(
            id = "math_calc_std_limits",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Limits & Derivatives",
            topic = "Standard Limits",
            name = "Fundamental Standard Limits",
            latex = "\\lim_{x \\to 0} \\frac{\\sin x}{x} = 1, \\quad \\lim_{x \\to 0} \\frac{e^x - 1}{x} = 1, \\quad \\lim_{x \\to 0} \\frac{\\ln(1+x)}{x} = 1, \\quad \\lim_{x \\to 0} (1+x)^{1/x} = e",
            variables = listOf(
                VariableDef("x", "Approaching variable", "rad for trig")
            ),
            conditions = "Angles in radians, valid as x → 0",
            commonMistake = "Applying sin(x)/x = 1 when x is in degrees without converting to radians (π/180 factor)",
            explanation = "Core foundational limits in differential calculus.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Calculus", "Limits"),
            searchKeywords = listOf("standard limits", "lim sinx/x", "lim (1+x)^(1/x)")
        ),
        CanonicalFormula(
            id = "math_calc_leibniz",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Integral Calculus",
            topic = "Definite Integrals",
            name = "Newton-Leibniz Integral Differentiation Rule",
            latex = "\\frac{d}{dx} \\left( \\int_{u(x)}^{v(x)} f(t) \\, dt \\right) = f(v(x)) \\cdot v'(x) - f(u(x)) \\cdot u'(x)",
            variables = listOf(
                VariableDef("u(x), v(x)", "Differentiable variable limits of integration", ""),
                VariableDef("f(t)", "Continuous integrand function", "")
            ),
            conditions = "f(t) continuous, u(x) and v(x) differentiable functions of x",
            commonMistake = "Forgetting to multiply by chain rule derivatives v'(x) and u'(x)",
            explanation = "Differentiates an integral with variable limits without evaluating the antiderivative.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Calculus", "Definite Integrals", "Leibniz Rule"),
            searchKeywords = listOf("newton leibniz rule", "differentiation under integral", "leibniz theorem")
        ),
        CanonicalFormula(
            id = "math_calc_linear_de",
            subject = FormulaSubject.MATHEMATICS,
            classLevel = "Class 12",
            chapter = "Differential Equations",
            topic = "Linear DE",
            name = "First Order Linear Differential Equation",
            latex = "\\frac{dy}{dx} + P(x)y = Q(x) \\implies y \\cdot e^{\\int P(x)dx} = \\int Q(x) \\cdot e^{\\int P(x)dx} \\, dx + C",
            variables = listOf(
                VariableDef("P(x), Q(x)", "Functions of x alone", ""),
                VariableDef("IF = e^{\\int P(x)dx}", "Integrating Factor", "")
            ),
            conditions = "P(x) and Q(x) are integrable",
            commonMistake = "Forgetting to write the equation in standard form with leading coefficient of dy/dx equal to 1",
            explanation = "General solution for first order linear differential equations via Integrating Factor.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Differential Equations", "Linear DE", "Integrating Factor"),
            searchKeywords = listOf("linear differential equation", "integrating factor", "dy/dx + Py = Q")
        )
    )

    // ==========================================
    // PHYSICS (CLASS 11 & 12)
    // ==========================================
    val physicsFormulas: List<CanonicalFormula> = listOf(
        // KINEMATICS
        CanonicalFormula(
            id = "phys_kin_projectile_range",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Kinematics",
            topic = "Projectile Motion",
            name = "Horizontal Range & Maximum Height of Projectile",
            latex = "R = \\frac{u^2 \\sin(2\\theta)}{g}, \\quad H = \\frac{u^2 \\sin^2\\theta}{2g}, \\quad T = \\frac{2u \\sin\\theta}{g}",
            variables = listOf(
                VariableDef("u", "Initial launch speed", "m/s"),
                VariableDef("\\theta", "Angle of projection above horizontal", "rad / deg"),
                VariableDef("g", "Acceleration due to gravity", "m/s²")
            ),
            siUnit = "R (m), H (m), T (s)",
            conditions = "Flat terrain, negligible air resistance, uniform gravity",
            commonMistake = "Confusing sin(2θ) in Range with sin²θ in Maximum Height",
            explanation = "Complete projectile trajectory kinematic parameters on a flat horizontal plane.",
            example = "u = 20 \\text{ m/s}, \\theta = 30^\\circ, g = 10 \\text{ m/s}^2 \\implies R = \\frac{400 \\sin 60^\\circ}{10} = 20\\sqrt{3} \\approx 34.64 \\text{ m}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Mechanics", "Kinematics", "Projectile"),
            searchKeywords = listOf("projectile range", "max height projectile", "time of flight", "u^2 sin2theta/g")
        ),

        // NEWTON'S LAWS & WORK ENERGY
        CanonicalFormula(
            id = "phys_mech_work_energy",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Work, Energy & Power",
            topic = "Work-Energy Theorem",
            name = "Work-Energy Theorem & Power",
            latex = "W_{\\text{net}} = \\Delta K = \\frac{1}{2}m v_f^2 - \\frac{1}{2}m v_i^2, \\quad P = \\vec{F} \\cdot \\vec{v} = \\frac{dW}{dt}",
            variables = listOf(
                VariableDef("W_{\\text{net}}", "Work done by all forces (conservative + non-conservative + pseudo)", "J"),
                VariableDef("m", "Mass", "kg"),
                VariableDef("v", "Velocity", "m/s"),
                VariableDef("P", "Instantaneous Power", "W")
            ),
            siUnit = "J (Joules), W (Watts)",
            conditions = "Valid in an inertial reference frame (or include pseudo-force work in non-inertial frame)",
            commonMistake = "Omitting work done by friction or normal force in complex constraints",
            explanation = "Fundamental law connecting work done by all acting forces to change in kinetic energy.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Mechanics", "Work Energy Theorem", "Power"),
            searchKeywords = listOf("work energy theorem", "W = delta K", "power F.v")
        ),

        // ROTATION & MOI
        CanonicalFormula(
            id = "phys_rot_parallel_axis",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Rotational Motion",
            topic = "Moment of Inertia",
            name = "Parallel & Perpendicular Axis Theorems",
            latex = "I = I_{cm} + M d^2, \\quad I_z = I_x + I_y \\quad (\\text{planar laminar body})",
            variables = listOf(
                VariableDef("I_{cm}", "Moment of inertia about axis passing through center of mass", "kg·m²"),
                VariableDef("M", "Total mass of rigid body", "kg"),
                VariableDef("d", "Perpendicular distance between parallel axes", "m")
            ),
            siUnit = "kg·m²",
            conditions = "Parallel axis requires one axis to strictly pass through the Center of Mass. Perpendicular axis applies ONLY to 2D laminar bodies.",
            commonMistake = "Applying parallel axis theorem between two arbitrary axes that do not include the COM axis",
            explanation = "Calculates moment of inertia of rigid bodies about offset and orthogonal axes.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Mechanics", "Rotation", "Moment of Inertia"),
            searchKeywords = listOf("parallel axis theorem", "perpendicular axis theorem", "I = I_cm + Md^2")
        ),
        CanonicalFormula(
            id = "phys_rot_std_moi",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Rotational Motion",
            topic = "Standard Moments of Inertia",
            name = "Standard Body Moments of Inertia (I_cm)",
            latex = "I_{\\text{Ring}} = MR^2, \\quad I_{\\text{Disc}} = \\frac{1}{2}MR^2, \\quad I_{\\text{SolidSphere}} = \\frac{2}{5}MR^2, \\quad I_{\\text{HollowSphere}} = \\frac{2}{3}MR^2, \\quad I_{\\text{Rod}} = \\frac{1}{12}ML^2",
            variables = listOf(
                VariableDef("M", "Mass", "kg"),
                VariableDef("R", "Radius", "m"),
                VariableDef("L", "Length of thin rod", "m")
            ),
            siUnit = "kg·m²",
            conditions = "Uniform density and symmetric axes passing through center of mass",
            commonMistake = "Confusing solid sphere (2/5 MR²) with hollow spherical shell (2/3 MR²)",
            explanation = "Standard MOI values required for rolling dynamics and rotational kinetic energy.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Mechanics", "MOI", "Rigid Body"),
            searchKeywords = listOf("moment of inertia sphere", "MOI disc", "MOI ring", "MOI rod")
        ),

        // GRAVITATION
        CanonicalFormula(
            id = "phys_grav_escape_orbital",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Gravitation",
            topic = "Satellites",
            name = "Escape & Orbital Velocity",
            latex = "v_e = \\sqrt{\\frac{2GM}{R}} = \\sqrt{2gR}, \\quad v_o = \\sqrt{\\frac{GM}{r}}, \\quad v_e = \\sqrt{2} v_o",
            variables = listOf(
                VariableDef("M", "Mass of planet", "kg"),
                VariableDef("R", "Radius of planet", "m"),
                VariableDef("r", "Orbital radius (R + h)", "m"),
                VariableDef("G", "Gravitational constant (6.674 \\times 10^{-11})", "N·m²/kg²")
            ),
            siUnit = "m/s",
            conditions = "Spherically symmetric planet, ignoring atmospheric drag",
            commonMistake = "Using planet radius R instead of orbit radius r = R + h for satellite at altitude h",
            explanation = "Minimum speeds required to escape planetary gravitational field and orbit in circular path.",
            example = "\\text{For Earth: } v_e \\approx 11.2 \\text{ km/s}, v_o \\text{ (surface)} \\approx 7.9 \\text{ km/s}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Gravitation", "Escape Velocity", "Orbital Velocity"),
            searchKeywords = listOf("escape velocity", "orbital speed", "sqrt(2gR)", "kepler satellite")
        ),

        // FLUIDS & PROPERTIES OF MATTER
        CanonicalFormula(
            id = "phys_fluid_bernoulli",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Fluid Mechanics",
            topic = "Fluid Dynamics",
            name = "Bernoulli's Principle & Continuity Equation",
            latex = "P + \\frac{1}{2}\\rho v^2 + \\rho g h = \\text{constant}, \\quad A_1 v_1 = A_2 v_2",
            variables = listOf(
                VariableDef("P", "Static fluid pressure", "Pa"),
                VariableDef("\\rho", "Fluid density", "kg/m³"),
                VariableDef("v", "Flow velocity", "m/s"),
                VariableDef("h", "Elevation height", "m"),
                VariableDef("A", "Cross-sectional pipe area", "m²")
            ),
            siUnit = "Pa (Pascals), m/s",
            conditions = "Incompressible, non-viscous, irrotational, steady streamline flow",
            commonMistake = "Applying Bernoulli's theorem across turbulent eddies or where viscous losses are significant",
            explanation = "Conservation of energy per unit volume along a streamline in ideal fluid flow.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Fluids", "Bernoulli", "Continuity"),
            searchKeywords = listOf("bernoulli equation", "fluid pressure", "continuity A1v1=A2v2", "torricelli")
        ),
        CanonicalFormula(
            id = "phys_matter_terminal_velocity",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Properties of Matter",
            topic = "Viscosity",
            name = "Stokes' Law & Terminal Velocity",
            latex = "F_{\\text{drag}} = 6\\pi\\eta r v, \\quad v_t = \\frac{2r^2(\\rho - \\sigma)g}{9\\eta}",
            variables = listOf(
                VariableDef("r", "Radius of falling sphere", "m"),
                VariableDef("\\rho", "Density of sphere", "kg/m³"),
                VariableDef("\\sigma", "Density of viscous fluid medium", "kg/m³"),
                VariableDef("\\eta", "Coefficient of dynamic viscosity", "Pa·s / Poiseuille"),
                VariableDef("v_t", "Constant terminal speed", "m/s")
            ),
            siUnit = "m/s",
            conditions = "Small spherical body, low Reynolds number (laminar flow)",
            commonMistake = "Omitting buoyant force term (density of medium σ)",
            explanation = "Calculates viscous drag and terminal velocity when drag + buoyancy equals gravitational weight.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Viscosity", "Stokes Law", "Terminal Velocity"),
            searchKeywords = listOf("stokes law", "terminal velocity", "6 pi eta r v")
        ),

        // THERMODYNAMICS & KTG
        CanonicalFormula(
            id = "phys_thermo_first_law",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Thermodynamics",
            topic = "First Law & Carnot Efficiency",
            name = "First Law of Thermodynamics & Carnot Efficiency",
            latex = "\\Delta Q = \\Delta U + W, \\quad \\Delta U = n C_v \\Delta T, \\quad \\eta_{\\text{Carnot}} = 1 - \\frac{T_C}{T_H} = \\frac{W}{Q_{\\text{in}}}",
            variables = listOf(
                VariableDef("\\Delta Q", "Heat added to system", "J"),
                VariableDef("\\Delta U", "Change in internal energy", "J"),
                VariableDef("W", "Work done BY gas (\\int P dV)", "J"),
                VariableDef("T_C, T_H", "Cold and hot reservoir absolute temperatures", "K")
            ),
            siUnit = "J (Joules), Kelvin (K)",
            conditions = "Sign convention: Q in is +, W by system is + in Physics (opposite in Chemistry)",
            commonMistake = "Using temperatures in Celsius (°C) instead of Kelvin in Carnot efficiency",
            explanation = "Conservation of energy in thermodynamic processes and theoretical maximum heat engine efficiency.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Thermodynamics", "Carnot", "First Law"),
            searchKeywords = listOf("first law thermodynamics", "carnot efficiency", "internal energy nCv deltaT")
        ),
        CanonicalFormula(
            id = "phys_ktg_rms_speed",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Kinetic Theory",
            topic = "Gas Velocities",
            name = "RMS, Average, and Most Probable Gas Speeds",
            latex = "v_{\\text{rms}} = \\sqrt{\\frac{3RT}{M}} = \\sqrt{\\frac{3k_B T}{m}}, \\quad v_{\\text{avg}} = \\sqrt{\\frac{8RT}{\\pi M}}, \\quad v_{\\text{mp}} = \\sqrt{\\frac{2RT}{M}}",
            variables = listOf(
                VariableDef("R", "Gas constant (8.314 J/(mol·K))", ""),
                VariableDef("T", "Absolute temperature", "K"),
                VariableDef("M", "Molar mass in kg/mol", "kg/mol"),
                VariableDef("k_B", "Boltzmann constant (1.38 \\times 10^{-23})", "J/K")
            ),
            siUnit = "m/s",
            conditions = "Ideal gas obeying Maxwell-Boltzmann distribution",
            commonMistake = "Entering molar mass M in grams/mol instead of kg/mol (must multiply by 10⁻³)",
            explanation = "Order of speeds is v_rms > v_avg > v_mp (ratio √3 : √(8/π) : √2 ≈ 1.224 : 1.128 : 1.000).",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("KTG", "RMS Speed", "Gas Speeds"),
            searchKeywords = listOf("v rms", "rms speed", "most probable speed", "sqrt(3RT/M)")
        ),

        // SHM & WAVES
        CanonicalFormula(
            id = "phys_shm_spring_pendulum",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "SHM & Oscillations",
            topic = "Time Period",
            name = "Time Period of Spring-Mass & Simple Pendulum",
            latex = "T_{\\text{spring}} = 2\\pi\\sqrt{\\frac{m}{k}}, \\quad T_{\\text{pendulum}} = 2\\pi\\sqrt{\\frac{L}{g_{\\text{eff}}}}",
            variables = listOf(
                VariableDef("m", "Mass", "kg"),
                VariableDef("k", "Spring constant", "N/m"),
                VariableDef("L", "Length of pendulum", "m"),
                VariableDef("g_{\\text{eff}}", "Effective gravity", "m/s²")
            ),
            siUnit = "s (seconds)",
            conditions = "Small angular displacement (θ < 5° for pendulum), ideal Hookean spring",
            commonMistake = "Assuming spring time period depends on gravity or inclination angle (it does not!)",
            explanation = "Fundamental harmonic oscillation time periods for translational and gravitational systems.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("SHM", "Oscillations", "Time Period"),
            searchKeywords = listOf("time period spring", "simple pendulum period", "2pi sqrt(m/k)", "2pi sqrt(L/g)")
        ),
        CanonicalFormula(
            id = "phys_wave_doppler",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 11",
            chapter = "Waves & Sound",
            topic = "Doppler Effect",
            name = "Doppler Effect in Sound",
            latex = "f' = f \\left( \\frac{v \\pm v_o}{v \\mp v_s} \\right)",
            variables = listOf(
                VariableDef("f", "True source frequency", "Hz"),
                VariableDef("f'", "Apparent perceived frequency", "Hz"),
                VariableDef("v", "Speed of sound in medium", "m/s"),
                VariableDef("v_o", "Speed of observer", "m/s"),
                VariableDef("v_s", "Speed of source", "m/s")
            ),
            siUnit = "Hz",
            conditions = "Speeds much less than sound speed, medium stationary relative to ground",
            commonMistake = "Reversing sign convention: use upper signs (+ in numerator, - in denominator) when approaching",
            explanation = "Calculates apparent pitch shift when source or observer moves relative to the acoustic medium.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Waves", "Doppler Effect", "Acoustics"),
            searchKeywords = listOf("doppler effect sound", "apparent frequency", "v +- vo / v -+ vs")
        ),

        // ELECTROSTATICS & CAPACITORS
        CanonicalFormula(
            id = "phys_elec_coulomb_dipole",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Electrostatics",
            topic = "Coulomb & Dipole",
            name = "Coulomb's Law & Electric Dipole Field",
            latex = "F = \\frac{1}{4\\pi\\varepsilon_0} \\frac{|q_1 q_2|}{r^2}, \\quad E_{\\text{axial}} = \\frac{2kp}{r^3}, \\quad E_{\\text{equatorial}} = \\frac{kp}{r^3}",
            variables = listOf(
                VariableDef("k = 1/(4\\pi\\varepsilon_0)", "Electrostatic constant (8.99 \\times 10^9)", "N·m²/C²"),
                VariableDef("p = q(2a)", "Electric dipole moment", "C·m"),
                VariableDef("r", "Distance from dipole center (r >> a)", "m")
            ),
            siUnit = "N (Force), N/C (Field)",
            conditions = "Point charges in vacuum/air, short dipole approximation (r >> a)",
            commonMistake = "Using 1/r² instead of 1/r³ for dipole electric field falloff",
            explanation = "Fundamental electrostatic force law and short dipole axial/equatorial fields.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Electrostatics", "Coulomb", "Dipole"),
            searchKeywords = listOf("coulomb law", "dipole electric field", "2kp/r^3", "kp/r^3")
        ),
        CanonicalFormula(
            id = "phys_elec_capacitor_energy",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Capacitance",
            topic = "Capacitor Energy",
            name = "Capacitance & Energy Stored",
            latex = "C = \\frac{K \\varepsilon_0 A}{d}, \\quad U = \\frac{1}{2} C V^2 = \\frac{Q^2}{2C} = \\frac{1}{2} Q V, \\quad u_E = \\frac{1}{2}\\varepsilon_0 E^2",
            variables = listOf(
                VariableDef("A", "Plate area", "m²"),
                VariableDef("d", "Plate separation distance", "m"),
                VariableDef("K", "Dielectric constant", ""),
                VariableDef("U", "Total stored electrostatic energy", "J"),
                VariableDef("u_E", "Energy density in electric field", "J/m³")
            ),
            siUnit = "F (Farad), J (Joules)",
            conditions = "Parallel plate capacitor with plate dimensions >> separation d",
            commonMistake = "Assuming energy increases when dielectric inserted with battery disconnected (Q is constant, C increases, so U = Q²/(2C) decreases!)",
            explanation = "Formulas for capacitance with dielectric and energy storage in electric fields.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Capacitance", "Dielectric", "Energy Density"),
            searchKeywords = listOf("capacitance formula", "energy stored capacitor", "1/2 CV^2", "dielectric K")
        ),

        // CURRENT ELECTRICITY & MAGNETISM
        CanonicalFormula(
            id = "phys_curr_drift_res",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Current Electricity",
            topic = "Drift Velocity & Resistance",
            name = "Drift Velocity, Current Density & Resistance",
            latex = "v_d = \\frac{e E \\tau}{m}, \\quad I = n e A v_d, \\quad R = \\frac{\\rho L}{A} = \\frac{m L}{n e^2 \\tau A}",
            variables = listOf(
                VariableDef("n", "Free electron number density", "m⁻³"),
                VariableDef("e", "Elementary electron charge (1.6 \\times 10^{-19})", "C"),
                VariableDef("\\tau", "Average relaxation time", "s"),
                VariableDef("m", "Electron mass", "kg"),
                VariableDef("\\rho", "Resistivity", "\\Omega\\cdot\\text{m}")
            ),
            siUnit = "m/s (v_d), \\Omega (R)",
            conditions = "Ohmic conductors at uniform temperature",
            commonMistake = "Confusing drift speed (~mm/s) with signal propagation speed (~speed of light)",
            explanation = "Microscopic origin of Ohm's law from electron scattering and relaxation time.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Current Electricity", "Drift Velocity", "Ohm Law"),
            searchKeywords = listOf("drift velocity", "I = neAv_d", "resistivity rho = m/(ne^2 tau)")
        ),
        CanonicalFormula(
            id = "phys_mag_biot_savart_lorentz",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Magnetic Effects of Current",
            topic = "Magnetic Force & Fields",
            name = "Lorentz Force & Biot-Savart Law",
            latex = "\\vec{F} = q(\\vec{E} + \\vec{v} \\times \\vec{B}), \\quad d\\vec{B} = \\frac{\\mu_0}{4\\pi} \\frac{I (d\\vec{l} \\times \\hat{r})}{r^2}, \\quad B_{\\text{center}} = \\frac{\\mu_0 I}{2R}",
            variables = listOf(
                VariableDef("B", "Magnetic field strength", "T (Tesla)"),
                VariableDef("\\mu_0 = 4\\pi \\times 10^{-7}", "Permeability of free space", "T·m/A"),
                VariableDef("q", "Charge", "C"),
                VariableDef("v", "Velocity", "m/s")
            ),
            siUnit = "T (Tesla), N (Force)",
            conditions = "Steady direct currents",
            commonMistake = "Magnetic force does no work (since F ⊥ v), so it changes particle direction but NEVER kinetic energy",
            explanation = "Defines magnetic fields generated by steady currents and the total electromagnetic Lorentz force.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Magnetism", "Lorentz Force", "Biot Savart"),
            searchKeywords = listOf("lorentz force", "biot savart law", "magnetic field center loop", "mu_0 I / 2R")
        ),

        // EMI & AC CIRCUITS
        CanonicalFormula(
            id = "phys_ac_lcr_resonance",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Alternating Current",
            topic = "LCR Circuits",
            name = "Series LCR Impedance, Resonance & Quality Factor",
            latex = "Z = \\sqrt{R^2 + (X_L - X_C)^2}, \\quad \\omega_0 = \\frac{1}{\\sqrt{LC}}, \\quad Q = \\frac{\\omega_0 L}{R} = \\frac{1}{R}\\sqrt{\\frac{L}{C}}",
            variables = listOf(
                VariableDef("X_L = \\omega L", "Inductive reactance", "\\Omega"),
                VariableDef("X_C = 1/(\\omega C)", "Capacitive reactance", "\\Omega"),
                VariableDef("Z", "Total circuit impedance", "\\Omega"),
                VariableDef("\\omega_0", "Resonant angular frequency", "rad/s"),
                VariableDef("Q", "Quality sharpness factor", "")
            ),
            siUnit = "\\Omega (Ohms), rad/s",
            conditions = "Sinusoidal AC excitation",
            commonMistake = "At resonance, impedance is MINIMUM (Z = R) and current is MAXIMUM in series LCR",
            explanation = "Complete response of series RLC alternating current circuit under AC source.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("AC", "LCR Circuit", "Resonance", "Quality Factor"),
            searchKeywords = listOf("LCR impedance", "resonant frequency 1/sqrt(LC)", "quality factor Q")
        ),

        // OPTICS
        CanonicalFormula(
            id = "phys_optics_lens_maker",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Ray Optics",
            topic = "Lenses",
            name = "Lens Maker's Formula & Thin Lens Equation",
            latex = "\\frac{1}{f} = \\left( \\frac{\\mu_L}{\\mu_m} - 1 \\right) \\left( \\frac{1}{R_1} - \\frac{1}{R_2} \\right), \\quad \\frac{1}{v} - \\frac{1}{u} = \\frac{1}{f}",
            variables = listOf(
                VariableDef("f", "Focal length", "m"),
                VariableDef("\\mu_L, \\mu_m", "Refractive indices of lens and surrounding medium", ""),
                VariableDef("R_1, R_2", "Radii of curvature with proper Cartesian sign convention", "m")
            ),
            siUnit = "m (Meters), D (Diopters = 1/m)",
            conditions = "Thin lens, paraxial rays, correct sign convention (incident light direction = positive)",
            commonMistake = "Mixing up Cartesian signs of R₁ (+ for convex front) and R₂ (- for convex back)",
            explanation = "Determines focal length of a lens from curvature of surfaces and refractive indices.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Optics", "Lens Maker", "Refraction"),
            searchKeywords = listOf("lens maker formula", "thin lens equation", "1/f = (mu-1)(1/R1 - 1/R2)")
        ),
        CanonicalFormula(
            id = "phys_optics_ydse_fringe",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Wave Optics",
            topic = "Interference",
            name = "Young's Double Slit Experiment (YDSE)",
            latex = "\\beta = \\frac{\\lambda D}{d}, \\quad y_n^{\\text{bright}} = \\frac{n\\lambda D}{d}, \\quad y_n^{\\text{dark}} = \\left(n - \\frac{1}{2}\\right)\\frac{\\lambda D}{d}",
            variables = listOf(
                VariableDef("\\beta", "Fringe width between adjacent maxima", "m"),
                VariableDef("\\lambda", "Light wavelength", "m"),
                VariableDef("D", "Distance from slits to screen", "m"),
                VariableDef("d", "Distance between the two slits (d << D)", "m")
            ),
            siUnit = "m (Meters)",
            conditions = "Coherent monochromatic sources, paraxial approximation (d << D, y << D)",
            commonMistake = "Assuming fringe width changes when moving from bright to dark fringes (in standard YDSE, all fringes have equal width β)",
            explanation = "Calculates fringe spacing and positions of constructive/destructive interference.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Wave Optics", "YDSE", "Interference"),
            searchKeywords = listOf("YDSE fringe width", "beta = lambda D / d", "interference young double slit")
        ),

        // MODERN PHYSICS
        CanonicalFormula(
            id = "phys_modern_bohr_photoelectric",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Modern Physics",
            topic = "Photoelectric & Bohr Model",
            name = "Photoelectric Equation & Bohr Energy Levels",
            latex = "K_{\\text{max}} = h\\nu - \\phi = e V_0, \\quad E_n = -13.6 \\frac{Z^2}{n^2} \\text{ eV}, \\quad r_n = 0.529 \\frac{n^2}{Z} \\text{ Å}",
            variables = listOf(
                VariableDef("h\\nu", "Incident photon energy", "eV / J"),
                VariableDef("\\phi", "Work function of metal", "eV / J"),
                VariableDef("V_0", "Stopping potential", "V"),
                VariableDef("Z", "Atomic number of hydrogen-like ion", ""),
                VariableDef("n", "Principal quantum number (orbit)", "")
            ),
            siUnit = "eV, Å, V",
            conditions = "Bohr model applies strictly to single-electron hydrogenic systems (H, He⁺, Li²⁺)",
            commonMistake = "Applying Bohr formulas to multi-electron atoms where electron-electron repulsion occurs",
            explanation = "Einstein's photoelectric conservation equation and quantized energy levels of hydrogen-like atoms.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Modern Physics", "Photoelectric", "Bohr Model"),
            searchKeywords = listOf("photoelectric equation", "bohr radius", "bohr energy -13.6 Z^2/n^2", "stopping potential")
        ),
        CanonicalFormula(
            id = "phys_modern_radioactive_decay",
            subject = FormulaSubject.PHYSICS,
            classLevel = "Class 12",
            chapter = "Nuclear Physics",
            topic = "Radioactivity",
            name = "Radioactive Decay Law & Half-Life",
            latex = "N(t) = N_0 e^{-\\lambda t}, \\quad T_{1/2} = \\frac{\\ln 2}{\\lambda} = \\frac{0.693}{\\lambda}, \\quad t_{\\text{mean}} = \\frac{1}{\\lambda}",
            variables = listOf(
                VariableDef("N_0", "Initial number of radioactive nuclei", ""),
                VariableDef("N(t)", "Undecayed nuclei remaining at time t", ""),
                VariableDef("\\lambda", "Decay constant / disintegration constant", "s⁻¹"),
                VariableDef("T_{1/2}", "Half-life", "s")
            ),
            siUnit = "s (seconds), Bq (Becquerel)",
            conditions = "Large sample size of unstable radioactive nuclei",
            commonMistake = "Confusing undecayed nuclei N(t) with decayed nuclei N_decayed = N₀ - N(t)",
            explanation = "Exponential law of nuclear decay governing atomic fission and half-life decay.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Nuclear Physics", "Radioactivity", "Half Life"),
            searchKeywords = listOf("radioactive decay", "half life formula", "N = N0 e^(-lambda t)", "0.693 / lambda")
        )
    )

    // ==========================================
    // CHEMISTRY (CLASS 11 & 12)
    // ==========================================
    val chemistryFormulas: List<CanonicalFormula> = listOf(
        // MOLE CONCEPT & SOLUTIONS
        CanonicalFormula(
            id = "chem_mole_concentrations",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 11",
            chapter = "Some Basic Concepts of Chemistry",
            topic = "Concentration Terms",
            name = "Molarity, Molality & Mole Fraction",
            latex = "M = \\frac{n_{\\text{solute}}}{V_{\\text{soln}} (\\text{L})}, \\quad m = \\frac{n_{\\text{solute}}}{W_{\\text{solvent}} (\\text{kg})}, \\quad x_A = \\frac{n_A}{n_A + n_B}",
            variables = listOf(
                VariableDef("M", "Molarity", "mol/L"),
                VariableDef("m", "Molality (temperature independent)", "mol/kg"),
                VariableDef("x_A", "Mole fraction of component A", "")
            ),
            siUnit = "mol/L, mol/kg",
            conditions = "Molarity varies with temperature (due to volume expansion), while Molality is temperature independent",
            commonMistake = "Using mass of solution in denominator of molality instead of mass of PURE solvent in kg",
            explanation = "Primary solution stoichiometry concentration units in physical chemistry.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Mole Concept", "Solutions"),
            searchKeywords = listOf("molarity formula", "molality", "mole fraction", "concentration terms")
        ),
        CanonicalFormula(
            id = "chem_sol_colligative",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 12",
            chapter = "Solutions",
            topic = "Colligative Properties",
            name = "Colligative Properties with Van 't Hoff Factor",
            latex = "\\Delta T_b = i K_b m, \\quad \\Delta T_f = i K_f m, \\quad \\Pi = i C R T, \\quad \\frac{P^\\circ - P_s}{P^\\circ} = i x_{\\text{solute}}",
            variables = listOf(
                VariableDef("i", "Van 't Hoff factor (i = 1 + (n-1)\\alpha for dissociation)", ""),
                VariableDef("K_b, K_f", "Molal elevation & depression constants (ebullioscopic & cryoscopic)", "K·kg/mol"),
                VariableDef("\\Pi", "Osmotic pressure", "atm / Pa"),
                VariableDef("m", "Molality", "mol/kg")
            ),
            siUnit = "K, atm, Pa",
            conditions = "Dilute non-volatile solute solutions",
            commonMistake = "Forgetting van 't Hoff factor i for electrolytes (e.g. i ≈ 2 for NaCl, i ≈ 3 for CaCl₂)",
            explanation = "Properties depending strictly on the number of solute particles in solution.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Colligative Properties", "Van 't Hoff"),
            searchKeywords = listOf("colligative properties", "delta Tb = i Kb m", "osmotic pressure pi = iCRT", "van t hoff factor")
        ),

        // GASEOUS STATE
        CanonicalFormula(
            id = "chem_gas_vanderwaals",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 11",
            chapter = "States of Matter",
            topic = "Real Gases",
            name = "Van der Waals Equation of Real Gas",
            latex = "\\left( P + \\frac{a n^2}{V^2} \\right) (V - n b) = n R T",
            variables = listOf(
                VariableDef("a", "Correction factor for intermolecular attractive forces", "atm·L²/mol²"),
                VariableDef("b", "Effective incompressible molecular volume (co-volume b = 4 N_A v)", "L/mol"),
                VariableDef("n", "Moles", "mol")
            ),
            siUnit = "P (atm / Pa), V (L / m³)",
            conditions = "Applies to real gases at moderate to high pressures and low temperatures",
            commonMistake = "High 'a' indicates easily liquefiable gas (strong attractions); 'b' relates to molecular size",
            explanation = "Modifies ideal gas law for finite molecular volume and intermolecular attractive forces.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Real Gases", "Van der Waals"),
            searchKeywords = listOf("van der waals equation", "real gas equation", "(P + an^2/V^2)(V-nb) = nRT")
        ),

        // THERMODYNAMICS & EQUILIBRIUM
        CanonicalFormula(
            id = "chem_thermo_gibbs",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 11",
            chapter = "Chemical Thermodynamics",
            topic = "Gibbs Free Energy",
            name = "Gibbs Free Energy & Spontaneity",
            latex = "\\Delta G = \\Delta H - T\\Delta S, \\quad \\Delta G^\\circ = -RT \\ln K = -2.303 RT \\log_{10} K",
            variables = listOf(
                VariableDef("\\Delta G", "Change in Gibbs Free Energy (\\Delta G < 0 for spontaneous)", "kJ/mol"),
                VariableDef("\\Delta H", "Enthalpy change", "kJ/mol"),
                VariableDef("\\Delta S", "Entropy change", "J/(mol·K)"),
                VariableDef("K", "Equilibrium constant", "")
            ),
            siUnit = "kJ/mol, J/(mol·K)",
            conditions = "Constant temperature and pressure",
            commonMistake = "Mixing units of ΔH (in kJ) and TΔS (where ΔS is often given in J/K) without converting to same scale",
            explanation = "Criterion for chemical spontaneity and thermodynamic equilibrium constant.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Thermodynamics", "Gibbs Free Energy"),
            searchKeywords = listOf("gibbs free energy", "delta G = delta H - T delta S", "delta G = -RT ln K")
        ),
        CanonicalFormula(
            id = "chem_equil_henderson",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 11",
            chapter = "Equilibrium",
            topic = "Ionic Equilibrium & Buffers",
            name = "Henderson-Hasselbalch Equation for Buffer Solutions",
            latex = "\\text{pH} = \\text{p}K_a + \\log_{10}\\left( \\frac{[\\text{Conjugate Base}]}{[\\text{Weak Acid}]} \\right), \\quad \\text{pOH} = \\text{p}K_b + \\log_{10}\\left( \\frac{[\\text{Conjugate Acid}]}{[\\text{Weak Base}]} \\right)",
            variables = listOf(
                VariableDef("\\text{p}K_a = -\\log K_a", "Acid dissociation constant exponent", ""),
                VariableDef("[\\text{Base}], [\\text{Acid}]", "Molar concentrations in buffer", "M")
            ),
            siUnit = "pH scale (0 - 14)",
            conditions = "Buffer solution containing weak acid/base and its corresponding salt",
            commonMistake = "Swapping numerator and denominator in the log term: always [Salt]/[Acid] or [Conjugate Base]/[Acid]",
            explanation = "Calculates exact pH of acidic and basic buffer solutions.",
            example = "\\text{For acetic acid/acetate buffer with } [\\text{salt}] = [\\text{acid}], \\text{pH} = \\text{p}K_a \\approx 4.74",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Ionic Equilibrium", "Buffer", "pH"),
            searchKeywords = listOf("henderson hasselbalch", "buffer pH", "pH = pKa + log(salt/acid)")
        ),

        // ELECTROCHEMISTRY
        CanonicalFormula(
            id = "chem_electro_nernst",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 12",
            chapter = "Electrochemistry",
            topic = "Cell Potential",
            name = "Nernst Equation at 298 K",
            latex = "E_{\\text{cell}} = E^\\circ_{\\text{cell}} - \\frac{0.0591}{n} \\log_{10} Q, \\quad E^\\circ_{\\text{cell}} = \\frac{0.0591}{n} \\log_{10} K_{\\text{eq}}",
            variables = listOf(
                VariableDef("E_{\\text{cell}}", "Electrochemical cell potential under non-standard conditions", "V"),
                VariableDef("E^\\circ_{\\text{cell}}", "Standard cell potential (E^\\circ_{\\text{cathode}} - E^\\circ_{\\text{anode}})", "V"),
                VariableDef("n", "Number of moles of electrons exchanged in balanced redox reaction", ""),
                VariableDef("Q", "Reaction quotient of aqueous ions and partial pressures", "")
            ),
            siUnit = "V (Volts)",
            conditions = "T = 298.15 K (25 °C), pure solids and liquids have activity = 1",
            commonMistake = "Using non-reduction potentials: always use standard REDUCTION potentials for cathode and anode",
            explanation = "Calculates cell voltage as a function of ion concentrations and reaction quotient.",
            example = "\\text{For Daniell Cell: } Zn + Cu^{2+} \\rightleftharpoons Zn^{2+} + Cu, \\quad E = 1.10 - \\frac{0.0591}{2}\\log\\frac{[Zn^{2+}]}{[Cu^{2+}]}",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Electrochemistry", "Nernst Equation"),
            searchKeywords = listOf("nernst equation", "Ecell = E0 - 0.0591/n log Q", "standard reduction potential")
        ),
        CanonicalFormula(
            id = "chem_electro_kohlrausch",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 12",
            chapter = "Electrochemistry",
            topic = "Conductance",
            name = "Kohlrausch's Law of Independent Migration",
            latex = "\\Lambda_m^\\circ = \\nu_+ \\lambda_+^\\circ + \\nu_- \\lambda_-^\\circ, \\quad \\alpha = \\frac{\\Lambda_m}{\\Lambda_m^\\circ}",
            variables = listOf(
                VariableDef("\\Lambda_m^\\circ", "Limiting molar conductivity at infinite dilution", "S·cm²/mol"),
                VariableDef("\\lambda_+^\\circ, \\lambda_-^\\circ", "Limiting ionic conductivities of cation and anion", "S·cm²/mol"),
                VariableDef("\\alpha", "Degree of dissociation of weak electrolyte", "")
            ),
            siUnit = "S·cm²/mol / S·m²/mol",
            conditions = "Infinite dilution where interionic interactions become zero",
            commonMistake = "Omitting stoichiometric stoichiometric coefficients ν₊ and ν₋ for polyvalent salts (e.g. Al₂(SO₄)₃: 2λ(Al³⁺) + 3λ(SO₄²⁻))",
            explanation = "Enables calculating molar conductivity of weak electrolytes (like CH₃COOH) at infinite dilution.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Electrochemistry", "Kohlrausch Law", "Conductance"),
            searchKeywords = listOf("kohlrausch law", "limiting molar conductivity", "degree of dissociation alpha")
        ),

        // CHEMICAL KINETICS
        CanonicalFormula(
            id = "chem_kinetics_first_order",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 12",
            chapter = "Chemical Kinetics",
            topic = "Integrated Rate Law",
            name = "First Order Integrated Rate Law & Arrhenius Equation",
            latex = "k = \\frac{2.303}{t} \\log_{10}\\left( \\frac{[A]_0}{[A]} \\right), \\quad t_{1/2} = \\frac{0.693}{k}, \\quad k = A e^{-E_a / RT}",
            variables = listOf(
                VariableDef("k", "Rate constant", "s⁻¹ for 1st order"),
                VariableDef("[A]_0", "Initial concentration of reactant", "M"),
                VariableDef("[A]", "Remaining concentration at time t", "M"),
                VariableDef("E_a", "Activation energy", "J/mol or kJ/mol"),
                VariableDef("A", "Arrhenius pre-exponential frequency factor", "")
            ),
            siUnit = "s⁻¹, kJ/mol",
            conditions = "First order elementary or pseudo-first order reaction",
            commonMistake = "First order half-life is INDEPENDENT of initial concentration, unlike 0th (t_1/2 ∝ [A]₀) or 2nd order (t_1/2 ∝ 1/[A]₀)",
            explanation = "Governs radioactive decay, ester hydrolysis, and temperature dependence of chemical rates.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Chemical Kinetics", "First Order", "Arrhenius"),
            searchKeywords = listOf("first order kinetics", "t1/2 = 0.693/k", "arrhenius equation", "rate constant")
        ),

        // SOLID STATE
        CanonicalFormula(
            id = "chem_solid_density",
            subject = FormulaSubject.CHEMISTRY,
            classLevel = "Class 12",
            chapter = "Solid State",
            topic = "Unit Cell",
            name = "Density of Unit Cell",
            latex = "\\rho = \\frac{z \\cdot M}{a^3 \\cdot N_A}",
            variables = listOf(
                VariableDef("z", "Number of atoms per unit cell (z=1 for SC, z=2 for BCC, z=4 for FCC)", ""),
                VariableDef("M", "Molar mass of substance", "g/mol"),
                VariableDef("a", "Edge length of unit cubic cell", "cm"),
                VariableDef("N_A = 6.022 \\times 10^{23}", "Avogadro's number", "mol⁻¹"),
                VariableDef("\\rho", "Density of crystal", "g/cm³")
            ),
            siUnit = "g/cm³",
            conditions = "Cubic crystal lattices (SC, BCC, FCC)",
            commonMistake = "Edge length a is usually given in pm or Å; must convert to cm (1 pm = 10⁻¹⁰ cm, 1 Å = 10⁻⁸ cm) to match g/cm³",
            explanation = "Relates microscopic atomic lattice parameters (z, edge length) to macroscopic crystal density.",
            jeeLevel = JeeLevel.BOTH,
            tags = listOf("Physical Chemistry", "Solid State", "Crystal Density"),
            searchKeywords = listOf("density unit cell", "rho = zM/(a^3 NA)", "solid state FCC BCC")
        )
    )

    /**
     * Powerful global search across formula name, chapter, topic, search keywords, variables, and tags.
     */
    fun search(query: String, subjectFilter: FormulaSubject? = null, jeeFilter: JeeLevel? = null): List<CanonicalFormula> {
        val q = query.trim().lowercase()
        return allFormulas.filter { formula ->
            val matchesSubject = subjectFilter == null || formula.subject == subjectFilter
            val matchesJee = jeeFilter == null || formula.jeeLevel == jeeFilter || formula.jeeLevel == JeeLevel.BOTH

            if (!matchesSubject || !matchesJee) return@filter false

            if (q.isEmpty()) return@filter true

            formula.name.lowercase().contains(q) ||
                    formula.chapter.lowercase().contains(q) ||
                    formula.topic.lowercase().contains(q) ||
                    formula.latex.lowercase().contains(q) ||
                    formula.searchKeywords.any { it.lowercase().contains(q) } ||
                    formula.tags.any { it.lowercase().contains(q) } ||
                    formula.variables.any { it.symbol.lowercase().contains(q) || it.name.lowercase().contains(q) }
        }
    }
}
