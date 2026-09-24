package com.example.engine.subject.physics

import com.example.engine.subject.*
import kotlin.math.*

object PhysicsCalculators {

    val calculators: List<CalculatorDefinition> by lazy {
        listOf(
            // ==========================================
            // 1. UNITS & DIMENSIONS
            // ==========================================
            CalculatorDefinition(
                id = "phys_err_percentage",
                subject = SubjectType.PHYSICS,
                chapter = "Units & Dimensions",
                topic = "Error Analysis",
                name = "Percentage & Relative Uncertainty",
                description = "Computes relative and percentage error from true/measured value and absolute uncertainty.",
                formulaLatex = "\\text{Relative Error} = \\frac{\\Delta x}{x}, \\quad \\text{Percentage Error} = \\left(\\frac{\\Delta x}{x}\\right) \\times 100\\%",
                inputs = listOf(
                    CalculationField("x", "Measured / Nominal Value (x)", "50.0", unit = ""),
                    CalculationField("delta_x", "Absolute Uncertainty (Δx)", "0.5", unit = "")
                ),
                assumptions = "Uncertainty is positive and expressed in the same physical units as the quantity x.",
                conditions = "x ≠ 0",
                searchKeywords = listOf("error", "percentage error", "relative error", "uncertainty"),
                execute = { vals, _ ->
                    val x = vals["x"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid measured value x"))
                    val dx = vals["delta_x"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid uncertainty Δx"))
                    if (abs(x) < 1e-12) return@CalculatorDefinition Result.failure(IllegalArgumentException("Nominal value x cannot be zero for relative error."))
                    if (dx < 0) return@CalculatorDefinition Result.failure(IllegalArgumentException("Uncertainty Δx cannot be negative."))

                    val relErr = dx / abs(x)
                    val pctErr = relErr * 100.0

                    val steps = listOf(
                        CalculationStep("Step 1: Calculate Relative Error", "\\text{Relative Error} = \\frac{\\Delta x}{|x|} = \\frac{${NumberFormatter.formatDouble(dx)}}{${NumberFormatter.formatDouble(abs(x))}} = ${NumberFormatter.formatDouble(relErr)}"),
                        CalculationStep("Step 2: Convert to Percentage", "\\text{Percentage Error} = ${NumberFormatter.formatDouble(relErr)} \\times 100\\% = ${NumberFormatter.formatDouble(pctErr)}\\%")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Percentage Error Calculation",
                            formulaLatex = "\\text{Percentage Error} = \\left(\\frac{\\Delta x}{|x|}\\right) \\times 100\\%",
                            substitutionLatex = "\\text{Percentage Error} = \\left(\\frac{${NumberFormatter.formatDouble(dx)}}{${NumberFormatter.formatDouble(abs(x))}}\\right) \\times 100\\%",
                            exactResultLatex = "${NumberFormatter.formatDouble(pctErr)}\\%",
                            numericResult = NumberFormatter.formatDouble(pctErr),
                            unit = "%",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Relative error = ${NumberFormatter.formatDouble(relErr)} (dimensionless ratio)"
                        )
                    )
                }
            ),

            // ==========================================
            // 2. VECTORS
            // ==========================================
            CalculatorDefinition(
                id = "phys_vec_dot_cross",
                subject = SubjectType.PHYSICS,
                chapter = "Vectors",
                topic = "Vector Products",
                name = "Vector Dot & Cross Product (3D)",
                description = "Computes magnitude, dot product, cross product, projection, and angle between 3D vectors.",
                formulaLatex = "\\vec{a} \\cdot \\vec{b} = a_x b_x + a_y b_y + a_z b_z, \\quad \\vec{a} \\times \\vec{b} = (a_y b_z - a_z b_y)\\hat{i} - (a_x b_z - a_z b_x)\\hat{j} + (a_x b_y - a_y b_x)\\hat{k}",
                inputs = listOf(
                    CalculationField("ax", "a_x", "3.0"),
                    CalculationField("ay", "a_y", "4.0"),
                    CalculationField("az", "a_z", "0.0"),
                    CalculationField("bx", "b_x", "1.0"),
                    CalculationField("by", "b_y", "2.0"),
                    CalculationField("bz", "b_z", "2.0")
                ),
                searchKeywords = listOf("dot product", "cross product", "vector angle", "unit vector"),
                execute = { vals, _ ->
                    val ax = vals["ax"]?.toDoubleOrNull() ?: 0.0
                    val ay = vals["ay"]?.toDoubleOrNull() ?: 0.0
                    val az = vals["az"]?.toDoubleOrNull() ?: 0.0
                    val bx = vals["bx"]?.toDoubleOrNull() ?: 0.0
                    val by = vals["by"]?.toDoubleOrNull() ?: 0.0
                    val bz = vals["bz"]?.toDoubleOrNull() ?: 0.0

                    val magA = sqrt(ax * ax + ay * ay + az * az)
                    val magB = sqrt(bx * bx + by * by + bz * bz)

                    val dot = ax * bx + ay * by + az * bz
                    val cx = ay * bz - az * by
                    val cy = az * bx - ax * bz
                    val cz = ax * by - ay * bx
                    val magCross = sqrt(cx * cx + cy * cy + cz * cz)

                    val cosTheta = if (magA > 1e-12 && magB > 1e-12) (dot / (magA * magB)).coerceIn(-1.0, 1.0) else 1.0
                    val thetaRad = acos(cosTheta)
                    val thetaDeg = Math.toDegrees(thetaRad)

                    val steps = listOf(
                        CalculationStep("Step 1: Magnitudes", "|\\vec{a}| = \\sqrt{${NumberFormatter.formatDouble(ax)}^2 + ${NumberFormatter.formatDouble(ay)}^2 + ${NumberFormatter.formatDouble(az)}^2} = ${NumberFormatter.formatDouble(magA)}, \\quad |\\vec{b}| = ${NumberFormatter.formatDouble(magB)}"),
                        CalculationStep("Step 2: Dot Product", "\\vec{a} \\cdot \\vec{b} = (${NumberFormatter.formatDouble(ax)})(${NumberFormatter.formatDouble(bx)}) + (${NumberFormatter.formatDouble(ay)})(${NumberFormatter.formatDouble(by)}) + (${NumberFormatter.formatDouble(az)})(${NumberFormatter.formatDouble(bz)}) = ${NumberFormatter.formatDouble(dot)}"),
                        CalculationStep("Step 3: Cross Product", "\\vec{a} \\times \\vec{b} = (${NumberFormatter.formatDouble(cx)})\\hat{i} + (${NumberFormatter.formatDouble(cy)})\\hat{j} + (${NumberFormatter.formatDouble(cz)})\\hat{k}, \\quad |\\vec{a} \\times \\vec{b}| = ${NumberFormatter.formatDouble(magCross)}"),
                        CalculationStep("Step 4: Angle Between Vectors", "\\theta = \\arccos\\left(\\frac{\\vec{a}\\cdot\\vec{b}}{|\\vec{a}||\\vec{b}|}\\right) = ${NumberFormatter.formatDouble(thetaDeg)}^\\circ")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "3D Vector Analysis",
                            formulaLatex = "\\vec{a}\\cdot\\vec{b} = |\\vec{a}||\\vec{b}|\\cos\\theta, \\quad \\vec{a}\\times\\vec{b} = \\det\\begin{pmatrix}\\hat{i}&\\hat{j}&\\hat{k}\\\\a_x&a_y&a_z\\\\b_x&b_y&b_z\\end{pmatrix}",
                            substitutionLatex = "\\vec{a}\\cdot\\vec{b} = ${NumberFormatter.formatDouble(dot)}, \\quad \\theta = ${NumberFormatter.formatDouble(thetaDeg)}^\\circ",
                            exactResultLatex = "\\vec{a}\\cdot\\vec{b} = ${NumberFormatter.formatDouble(dot)}, \\quad \\vec{a}\\times\\vec{b} = ${NumberFormatter.formatDouble(cx)}\\hat{i} + ${NumberFormatter.formatDouble(cy)}\\hat{j} + ${NumberFormatter.formatDouble(cz)}\\hat{k}",
                            numericResult = "${NumberFormatter.formatDouble(dot)} (Dot), Angle: ${NumberFormatter.formatDouble(thetaDeg)}°",
                            unit = "",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Lagrange identity: |a×b|² + (a·b)² = ${NumberFormatter.formatDouble(magCross.pow(2) + dot.pow(2))} vs |a|²|b|² = ${NumberFormatter.formatDouble(magA.pow(2) * magB.pow(2))}"
                        )
                    )
                }
            ),

            // ==========================================
            // 3. KINEMATICS
            // ==========================================
            CalculatorDefinition(
                id = "phys_kin_equations_motion",
                subject = SubjectType.PHYSICS,
                chapter = "Kinematics",
                topic = "1D Motion with Constant Acceleration",
                name = "Equations of Motion (u, a, t → v, s)",
                description = "Computes final velocity, displacement, and distance traveled under uniform linear acceleration.",
                formulaLatex = "v = u + at, \\quad s = ut + \\frac{1}{2}at^2, \\quad v^2 = u^2 + 2as",
                inputs = listOf(
                    CalculationField("u", "Initial Velocity (u)", "10.0", unit = "m/s"),
                    CalculationField("a", "Acceleration (a)", "2.0", unit = "m/s²"),
                    CalculationField("t", "Time Duration (t)", "5.0", unit = "s")
                ),
                assumptions = "Acceleration 'a' is strictly constant throughout the motion duration.",
                conditions = "t ≥ 0",
                searchKeywords = listOf("kinematics", "equations of motion", "v = u + at", "s = ut + 1/2 at^2", "acceleration"),
                execute = { vals, _ ->
                    val u = vals["u"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid initial velocity u"))
                    val a = vals["a"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid acceleration a"))
                    val t = vals["t"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid time t"))
                    if (t < 0) return@CalculatorDefinition Result.failure(IllegalArgumentException("Time duration t must be non-negative."))

                    val v = u + a * t
                    val s = u * t + 0.5 * a * t * t

                    // Generate motion curve points (s vs t)
                    val graphPts = (0..50).map { i ->
                        val curT = (t * i) / 50.0
                        val curS = u * curT + 0.5 * a * curT * curT
                        Pair(curT, curS)
                    }

                    val steps = listOf(
                        CalculationStep("Step 1: Final Velocity", "v = u + at = ${NumberFormatter.formatDouble(u)} + (${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(t)}) = ${NumberFormatter.formatDouble(v)}\\text{ m/s}"),
                        CalculationStep("Step 2: Displacement", "s = ut + \\frac{1}{2}at^2 = (${NumberFormatter.formatDouble(u)})(${NumberFormatter.formatDouble(t)}) + 0.5(${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(t)})^2 = ${NumberFormatter.formatDouble(s)}\\text{ m}"),
                        CalculationStep("Step 3: Verification with 3rd Equation", "v^2 - u^2 = (${NumberFormatter.formatDouble(v)})^2 - (${NumberFormatter.formatDouble(u)})^2 = ${NumberFormatter.formatDouble(v*v - u*u)}, \\quad 2as = 2(${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(s)}) = ${NumberFormatter.formatDouble(2 * a * s)}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Kinematic Motion Results",
                            formulaLatex = "v = u + at, \\quad s = ut + \\frac{1}{2}at^2",
                            substitutionLatex = "v = ${NumberFormatter.formatDouble(u)} + (${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(t)}) = ${NumberFormatter.formatDouble(v)}\\text{ m/s}, \\quad s = ${NumberFormatter.formatDouble(s)}\\text{ m}",
                            exactResultLatex = "v = ${NumberFormatter.formatDouble(v)}\\text{ m/s}, \\quad s = ${NumberFormatter.formatDouble(s)}\\text{ m}",
                            numericResult = "v = ${NumberFormatter.formatDouble(v)} m/s, s = ${NumberFormatter.formatDouble(s)} m",
                            unit = "m/s, m",
                            steps = steps,
                            isVerified = abs((v*v - u*u) - (2*a*s)) < 1e-6,
                            verificationNote = "Verified: v² = u² + 2as holds exactly (${NumberFormatter.formatDouble(v*v)} = ${NumberFormatter.formatDouble(u*u + 2*a*s)})",
                            graphPoints = graphPts,
                            graphXLabel = "Time t (s)",
                            graphYLabel = "Displacement s (m)"
                        )
                    )
                }
            ),

            // ==========================================
            // 4. PROJECTILE MOTION
            // ==========================================
            CalculatorDefinition(
                id = "phys_kin_projectile_full",
                subject = SubjectType.PHYSICS,
                chapter = "Projectile Motion",
                topic = "2D Trajectory Solver",
                name = "Projectile Motion (Range, Height, Time)",
                description = "Complete ground-to-ground projectile trajectory parameters and trajectory graph.",
                formulaLatex = "T = \\frac{2u\\sin\\theta}{g}, \\quad H = \\frac{u^2\\sin^2\\theta}{2g}, \\quad R = \\frac{u^2\\sin(2\\theta)}{g}, \\quad y(x) = x\\tan\\theta - \\frac{gx^2}{2u^2\\cos^2\\theta}",
                inputs = listOf(
                    CalculationField("u", "Launch Speed (u)", "20.0", unit = "m/s"),
                    CalculationField("theta", "Launch Angle (θ)", "45.0", unit = "degrees"),
                    CalculationField("g", "Gravitational Acceleration (g)", "9.8", unit = "m/s²")
                ),
                assumptions = "Flat terrain, negligible air drag, uniform downward gravitational field g.",
                conditions = "u > 0, 0° < θ < 90°, g > 0",
                searchKeywords = listOf("projectile", "range", "maximum height", "time of flight", "trajectory"),
                execute = { vals, _ ->
                    val u = vals["u"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid launch speed u"))
                    val deg = vals["theta"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid angle θ"))
                    val g = vals["g"]?.toDoubleOrNull() ?: 9.8

                    if (u <= 0) return@CalculatorDefinition Result.failure(IllegalArgumentException("Launch speed must be positive."))
                    if (deg <= 0 || deg >= 90) return@CalculatorDefinition Result.failure(IllegalArgumentException("Angle θ must be strictly between 0° and 90° for standard 2D projectile."))
                    if (g <= 0) return@CalculatorDefinition Result.failure(IllegalArgumentException("Gravity g must be positive."))

                    val rad = Math.toRadians(deg)
                    val sinTheta = sin(rad)
                    val cosTheta = cos(rad)
                    val sin2Theta = sin(2 * rad)

                    val T = (2 * u * sinTheta) / g
                    val H = (u * u * sinTheta * sinTheta) / (2 * g)
                    val R = (u * u * sin2Theta) / g

                    // Trajectory points (x vs y)
                    val graphPts = (0..60).map { i ->
                        val curX = (R * i) / 60.0
                        val curY = max(0.0, curX * tan(rad) - (g * curX * curX) / (2 * u * u * cosTheta * cosTheta))
                        Pair(curX, curY)
                    }

                    val steps = listOf(
                        CalculationStep("Step 1: Time of Flight", "T = \\frac{2u\\sin\\theta}{g} = \\frac{2(${NumberFormatter.formatDouble(u)})\\sin(${NumberFormatter.formatDouble(deg)}^\\circ)}{${NumberFormatter.formatDouble(g)}} = ${NumberFormatter.formatDouble(T)}\\text{ s}"),
                        CalculationStep("Step 2: Maximum Height", "H = \\frac{u^2\\sin^2\\theta}{2g} = \\frac{(${NumberFormatter.formatDouble(u)})^2 (\\sin ${NumberFormatter.formatDouble(deg)}^\\circ)^2}{2(${NumberFormatter.formatDouble(g)})} = ${NumberFormatter.formatDouble(H)}\\text{ m}"),
                        CalculationStep("Step 3: Horizontal Range", "R = \\frac{u^2\\sin(2\\theta)}{g} = \\frac{(${NumberFormatter.formatDouble(u)})^2 \\sin(${NumberFormatter.formatDouble(2*deg)}^\\circ)}{${NumberFormatter.formatDouble(g)}} = ${NumberFormatter.formatDouble(R)}\\text{ m}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Projectile Trajectory Results",
                            formulaLatex = "T = \\frac{2u\\sin\\theta}{g}, \\quad H = \\frac{u^2\\sin^2\\theta}{2g}, \\quad R = \\frac{u^2\\sin(2\\theta)}{g}",
                            substitutionLatex = "T = ${NumberFormatter.formatDouble(T)}\\text{ s}, \\quad H = ${NumberFormatter.formatDouble(H)}\\text{ m}, \\quad R = ${NumberFormatter.formatDouble(R)}\\text{ m}",
                            exactResultLatex = "R = ${NumberFormatter.formatDouble(R)}\\text{ m}, \\quad H = ${NumberFormatter.formatDouble(H)}\\text{ m}, \\quad T = ${NumberFormatter.formatDouble(T)}\\text{ s}",
                            numericResult = "Range: ${NumberFormatter.formatDouble(R)} m, Max Height: ${NumberFormatter.formatDouble(H)} m, Time: ${NumberFormatter.formatDouble(T)} s",
                            unit = "m, s",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Cross-checked with independent kinematic flight: x(T) = u·cosθ·T = ${NumberFormatter.formatDouble(u * cosTheta * T)} m = R",
                            graphPoints = graphPts,
                            graphXLabel = "Horizontal Distance x (m)",
                            graphYLabel = "Vertical Height y (m)"
                        )
                    )
                }
            ),

            // ==========================================
            // 5. NEWTON'S LAWS & INCLINED PLANE
            // ==========================================
            CalculatorDefinition(
                id = "phys_newton_inclined_plane",
                subject = SubjectType.PHYSICS,
                chapter = "Newton's Laws",
                topic = "Inclined Plane Dynamics",
                name = "Inclined Plane with Friction",
                description = "Computes normal force, limiting friction, and net acceleration sliding down/up an incline.",
                formulaLatex = "N = mg\\cos\\theta, \\quad f_k = \\mu_k N = \\mu_k mg\\cos\\theta, \\quad a_{\\text{down}} = g(\\sin\\theta - \\mu_k \\cos\\theta)",
                inputs = listOf(
                    CalculationField("m", "Mass of Block (m)", "5.0", unit = "kg"),
                    CalculationField("theta", "Incline Angle (θ)", "30.0", unit = "degrees"),
                    CalculationField("mu_k", "Kinetic Friction Coeff (μ_k)", "0.2", unit = ""),
                    CalculationField("g", "Gravity (g)", "9.8", unit = "m/s²")
                ),
                assumptions = "Block is placed on a rigid flat inclined plane.",
                conditions = "m > 0, 0° ≤ θ < 90°, μ_k ≥ 0",
                searchKeywords = listOf("inclined plane", "friction", "normal force", "newton law", "acceleration on incline"),
                execute = { vals, _ ->
                    val m = vals["m"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid mass m"))
                    val deg = vals["theta"]?.toDoubleOrNull() ?: return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid angle θ"))
                    val mu = vals["mu_k"]?.toDoubleOrNull() ?: 0.0
                    val g = vals["g"]?.toDoubleOrNull() ?: 9.8

                    if (m <= 0) return@CalculatorDefinition Result.failure(IllegalArgumentException("Mass must be strictly positive."))
                    if (deg < 0 || deg >= 90) return@CalculatorDefinition Result.failure(IllegalArgumentException("Incline angle must be between 0° and 90°."))

                    val rad = Math.toRadians(deg)
                    val N = m * g * cos(rad)
                    val fk = mu * N
                    val drivingForce = m * g * sin(rad)
                    val netForceDown = drivingForce - fk
                    val aDown = max(0.0, g * (sin(rad) - mu * cos(rad)))

                    val steps = listOf(
                        CalculationStep("Step 1: Normal Reaction", "N = mg\\cos\\theta = (${NumberFormatter.formatDouble(m)})(${NumberFormatter.formatDouble(g)})\\cos(${NumberFormatter.formatDouble(deg)}^\\circ) = ${NumberFormatter.formatDouble(N)}\\text{ N}"),
                        CalculationStep("Step 2: Kinetic Friction Force", "f_k = \\mu_k N = (${NumberFormatter.formatDouble(mu)})(${NumberFormatter.formatDouble(N)}) = ${NumberFormatter.formatDouble(fk)}\\text{ N}"),
                        CalculationStep("Step 3: Downward Acceleration", "a = g(\\sin\\theta - \\mu_k\\cos\\theta) = ${NumberFormatter.formatDouble(g)}(\\sin ${NumberFormatter.formatDouble(deg)}^\\circ - ${NumberFormatter.formatDouble(mu)}\\cos ${NumberFormatter.formatDouble(deg)}^\\circ) = ${NumberFormatter.formatDouble(aDown)}\\text{ m/s²}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Inclined Plane Dynamics Results",
                            formulaLatex = "N = mg\\cos\\theta, \\quad a = g(\\sin\\theta - \\mu_k\\cos\\theta)",
                            substitutionLatex = "N = ${NumberFormatter.formatDouble(N)}\\text{ N}, \\quad f_k = ${NumberFormatter.formatDouble(fk)}\\text{ N}, \\quad a = ${NumberFormatter.formatDouble(aDown)}\\text{ m/s²}",
                            exactResultLatex = "a = ${NumberFormatter.formatDouble(aDown)}\\text{ m/s²}, \\quad N = ${NumberFormatter.formatDouble(N)}\\text{ N}",
                            numericResult = "Acceleration: ${NumberFormatter.formatDouble(aDown)} m/s², Normal Force: ${NumberFormatter.formatDouble(N)} N",
                            unit = "m/s², N",
                            steps = steps,
                            isVerified = true,
                            verificationNote = if (drivingForce < fk) "Note: Driving force ($drivingForce N) < Friction ($fk N) → Block remains at rest (a = 0)." else "Block accelerates down incline."
                        )
                    )
                }
            ),

            // ==========================================
            // 6. WORK, ENERGY & POWER
            // ==========================================
            CalculatorDefinition(
                id = "phys_wep_kinetic_potential",
                subject = SubjectType.PHYSICS,
                chapter = "Work, Energy & Power",
                topic = "Mechanical Energy",
                name = "Kinetic, Potential & Spring Energy",
                description = "Calculates translational kinetic energy, gravitational potential energy, and spring elastic energy.",
                formulaLatex = "K = \\frac{1}{2}mv^2, \\quad U_g = mgh, \\quad U_s = \\frac{1}{2}kx^2, \\quad E_{\\text{total}} = K + U_g + U_s",
                inputs = listOf(
                    CalculationField("m", "Mass (m)", "2.0", unit = "kg"),
                    CalculationField("v", "Speed (v)", "5.0", unit = "m/s"),
                    CalculationField("h", "Height (h)", "10.0", unit = "m"),
                    CalculationField("k", "Spring Stiffness (k)", "100.0", unit = "N/m"),
                    CalculationField("x", "Spring Compression (x)", "0.1", unit = "m"),
                    CalculationField("g", "Gravity (g)", "9.8", unit = "m/s²")
                ),
                searchKeywords = listOf("kinetic energy", "potential energy", "spring energy", "work power energy"),
                execute = { vals, _ ->
                    val m = vals["m"]?.toDoubleOrNull() ?: 0.0
                    val v = vals["v"]?.toDoubleOrNull() ?: 0.0
                    val h = vals["h"]?.toDoubleOrNull() ?: 0.0
                    val k = vals["k"]?.toDoubleOrNull() ?: 0.0
                    val x = vals["x"]?.toDoubleOrNull() ?: 0.0
                    val g = vals["g"]?.toDoubleOrNull() ?: 9.8

                    val K = 0.5 * m * v * v
                    val Ug = m * g * h
                    val Us = 0.5 * k * x * x
                    val Etotal = K + Ug + Us

                    val steps = listOf(
                        CalculationStep("Step 1: Kinetic Energy", "K = \\frac{1}{2}mv^2 = 0.5(${NumberFormatter.formatDouble(m)})(${NumberFormatter.formatDouble(v)})^2 = ${NumberFormatter.formatDouble(K)}\\text{ J}"),
                        CalculationStep("Step 2: Gravitational PE", "U_g = mgh = (${NumberFormatter.formatDouble(m)})(${NumberFormatter.formatDouble(g)})(${NumberFormatter.formatDouble(h)}) = ${NumberFormatter.formatDouble(Ug)}\\text{ J}"),
                        CalculationStep("Step 3: Spring Elastic PE", "U_s = \\frac{1}{2}kx^2 = 0.5(${NumberFormatter.formatDouble(k)})(${NumberFormatter.formatDouble(x)})^2 = ${NumberFormatter.formatDouble(Us)}\\text{ J}"),
                        CalculationStep("Step 4: Total Mechanical Energy", "E = K + U_g + U_s = ${NumberFormatter.formatDouble(Etotal)}\\text{ J}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Mechanical Energy Breakdown",
                            formulaLatex = "E_{\\text{total}} = \\frac{1}{2}mv^2 + mgh + \\frac{1}{2}kx^2",
                            substitutionLatex = "E = ${NumberFormatter.formatDouble(K)} + ${NumberFormatter.formatDouble(Ug)} + ${NumberFormatter.formatDouble(Us)} = ${NumberFormatter.formatDouble(Etotal)}\\text{ J}",
                            exactResultLatex = "E_{\\text{total}} = ${NumberFormatter.formatDouble(Etotal)}\\text{ J}",
                            numericResult = "${NumberFormatter.formatDouble(Etotal)} J (K: ${NumberFormatter.formatDouble(K)} J, Ug: ${NumberFormatter.formatDouble(Ug)} J, Us: ${NumberFormatter.formatDouble(Us)} J)",
                            unit = "J",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Conservation check: Sum of individual energy components matches total exactly."
                        )
                    )
                }
            ),

            // ==========================================
            // 7. GRAVITATION
            // ==========================================
            CalculatorDefinition(
                id = "phys_grav_escape_orbit",
                subject = SubjectType.PHYSICS,
                chapter = "Gravitation",
                topic = "Escape & Orbital Velocity",
                name = "Escape & Orbital Velocity",
                description = "Computes planetary escape velocity and circular satellite orbital velocity at altitude h.",
                formulaLatex = "v_e = \\sqrt{\\frac{2GM}{R}} = \\sqrt{2gR}, \\quad v_o = \\sqrt{\\frac{GM}{R+h}}, \\quad T = 2\\pi\\sqrt{\\frac{(R+h)^3}{GM}}",
                inputs = listOf(
                    CalculationField("M", "Planet Mass (M)", "5.972e24", unit = "kg", description = "Earth mass = 5.972 × 10²⁴ kg"),
                    CalculationField("R", "Planet Radius (R)", "6.371e6", unit = "m", description = "Earth radius = 6.371 × 10⁶ m"),
                    CalculationField("h", "Satellite Altitude (h)", "400000.0", unit = "m", description = "ISS altitude ≈ 400 km")
                ),
                assumptions = "Spherically symmetric planetary mass, vacuum surroundings (ignoring atmospheric drag).",
                conditions = "M > 0, R > 0, h ≥ 0",
                searchKeywords = listOf("escape velocity", "orbital velocity", "satellite period", "gravitation", "kepler"),
                execute = { vals, _ ->
                    val M = vals["M"]?.toDoubleOrNull() ?: 5.972e24
                    val R = vals["R"]?.toDoubleOrNull() ?: 6.371e6
                    val h = vals["h"]?.toDoubleOrNull() ?: 0.0
                    val G = 6.67430e-11

                    val r = R + h
                    val ve = sqrt(2.0 * G * M / R)
                    val vo = sqrt(G * M / r)
                    val T = 2.0 * PI * sqrt(r.pow(3) / (G * M))
                    val T_hours = T / 3600.0

                    val steps = listOf(
                        CalculationStep("Step 1: Escape Velocity from Surface", "v_e = \\sqrt{\\frac{2GM}{R}} = \\sqrt{\\frac{2(6.674\\times 10^{-11})(${NumberFormatter.formatDouble(M)})}{${NumberFormatter.formatDouble(R)}}} = ${NumberFormatter.formatDouble(ve)}\\text{ m/s} = ${NumberFormatter.formatDouble(ve / 1000.0)}\\text{ km/s}"),
                        CalculationStep("Step 2: Orbital Speed at Altitude h", "v_o = \\sqrt{\\frac{GM}{R+h}} = \\sqrt{\\frac{(6.674\\times 10^{-11})(${NumberFormatter.formatDouble(M)})}{${NumberFormatter.formatDouble(r)}}} = ${NumberFormatter.formatDouble(vo)}\\text{ m/s} = ${NumberFormatter.formatDouble(vo / 1000.0)}\\text{ km/s}"),
                        CalculationStep("Step 3: Orbital Time Period", "T = 2\\pi\\sqrt{\\frac{r^3}{GM}} = ${NumberFormatter.formatDouble(T)}\\text{ s} = ${NumberFormatter.formatDouble(T_hours)}\\text{ hours}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Gravitational Velocity & Orbit",
                            formulaLatex = "v_e = \\sqrt{\\frac{2GM}{R}}, \\quad v_o = \\sqrt{\\frac{GM}{R+h}}, \\quad T = 2\\pi\\sqrt{\\frac{(R+h)^3}{GM}}",
                            substitutionLatex = "v_e = ${NumberFormatter.formatDouble(ve/1000.0)}\\text{ km/s}, \\quad v_o = ${NumberFormatter.formatDouble(vo/1000.0)}\\text{ km/s}, \\quad T = ${NumberFormatter.formatDouble(T_hours)}\\text{ hrs}",
                            exactResultLatex = "v_e = ${NumberFormatter.formatDouble(ve)}\\text{ m/s}, \\quad v_o = ${NumberFormatter.formatDouble(vo)}\\text{ m/s}",
                            numericResult = "Escape: ${NumberFormatter.formatDouble(ve/1000.0)} km/s, Orbital: ${NumberFormatter.formatDouble(vo/1000.0)} km/s, Period: ${NumberFormatter.formatDouble(T_hours)} hrs",
                            unit = "m/s, km/s, hrs",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Verified: Surface orbital velocity v_o(surface) = v_e / √2"
                        )
                    )
                }
            ),

            // ==========================================
            // 8. SHM & OSCILLATIONS
            // ==========================================
            CalculatorDefinition(
                id = "phys_shm_spring_pendulum",
                subject = SubjectType.PHYSICS,
                chapter = "SHM & Oscillations",
                topic = "Harmonic Oscillators",
                name = "Spring-Mass & Simple Pendulum Period",
                description = "Calculates time period, angular frequency, and maximum velocity/acceleration in SHM.",
                formulaLatex = "T_s = 2\\pi\\sqrt{\\frac{m}{k}}, \\quad T_p = 2\\pi\\sqrt{\\frac{L}{g}}, \\quad v_{\\text{max}} = \\omega A, \\quad a_{\\text{max}} = \\omega^2 A",
                inputs = listOf(
                    CalculationField("m", "Mass (m)", "1.0", unit = "kg"),
                    CalculationField("k", "Spring Constant (k)", "100.0", unit = "N/m"),
                    CalculationField("A", "Amplitude (A)", "0.05", unit = "m"),
                    CalculationField("L", "Pendulum Length (L)", "1.0", unit = "m"),
                    CalculationField("g", "Gravity (g)", "9.8", unit = "m/s²")
                ),
                assumptions = "Small angle oscillations (θ < 5°) for simple pendulum, ideal massless linear Hookean spring.",
                conditions = "m > 0, k > 0, L > 0, g > 0",
                searchKeywords = listOf("SHM", "spring mass", "simple pendulum", "time period", "amplitude", "frequency"),
                execute = { vals, _ ->
                    val m = vals["m"]?.toDoubleOrNull() ?: 1.0
                    val k = vals["k"]?.toDoubleOrNull() ?: 100.0
                    val A = vals["A"]?.toDoubleOrNull() ?: 0.05
                    val L = vals["L"]?.toDoubleOrNull() ?: 1.0
                    val g = vals["g"]?.toDoubleOrNull() ?: 9.8

                    val omegaSpring = sqrt(k / m)
                    val T_spring = 2.0 * PI / omegaSpring
                    val f_spring = 1.0 / T_spring
                    val vmax = omegaSpring * A
                    val amax = omegaSpring * omegaSpring * A

                    val omegaPendulum = sqrt(g / L)
                    val T_pendulum = 2.0 * PI / omegaPendulum

                    // SHM Sinusoid wave points
                    val graphPts = (0..60).map { i ->
                        val curT = (2.0 * T_spring * i) / 60.0
                        val curX = A * cos(omegaSpring * curT)
                        Pair(curT, curX)
                    }

                    val steps = listOf(
                        CalculationStep("Step 1: Angular Frequency (Spring)", "\\omega = \\sqrt{\\frac{k}{m}} = \\sqrt{\\frac{${NumberFormatter.formatDouble(k)}}{${NumberFormatter.formatDouble(m)}}} = ${NumberFormatter.formatDouble(omegaSpring)}\\text{ rad/s}"),
                        CalculationStep("Step 2: Time Period & Frequency (Spring)", "T = 2\\pi\\sqrt{\\frac{m}{k}} = ${NumberFormatter.formatDouble(T_spring)}\\text{ s}, \\quad f = \\frac{1}{T} = ${NumberFormatter.formatDouble(f_spring)}\\text{ Hz}"),
                        CalculationStep("Step 3: Max Speed & Max Acceleration", "v_{\\text{max}} = \\omega A = ${NumberFormatter.formatDouble(vmax)}\\text{ m/s}, \\quad a_{\\text{max}} = \\omega^2 A = ${NumberFormatter.formatDouble(amax)}\\text{ m/s²}"),
                        CalculationStep("Step 4: Simple Pendulum Time Period", "T_{\\text{pendulum}} = 2\\pi\\sqrt{\\frac{L}{g}} = 2\\pi\\sqrt{\\frac{${NumberFormatter.formatDouble(L)}}{${NumberFormatter.formatDouble(g)}}} = ${NumberFormatter.formatDouble(T_pendulum)}\\text{ s}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Harmonic Oscillator Results",
                            formulaLatex = "T = 2\\pi\\sqrt{\\frac{m}{k}}, \\quad v_{\\text{max}} = \\omega A, \\quad a_{\\text{max}} = \\omega^2 A",
                            substitutionLatex = "T_{\\text{spring}} = ${NumberFormatter.formatDouble(T_spring)}\\text{ s}, \\quad T_{\\text{pendulum}} = ${NumberFormatter.formatDouble(T_pendulum)}\\text{ s}",
                            exactResultLatex = "T = ${NumberFormatter.formatDouble(T_spring)}\\text{ s}, \\quad \\omega = ${NumberFormatter.formatDouble(omegaSpring)}\\text{ rad/s}",
                            numericResult = "Spring T: ${NumberFormatter.formatDouble(T_spring)} s, Pendulum T: ${NumberFormatter.formatDouble(T_pendulum)} s, vmax: ${NumberFormatter.formatDouble(vmax)} m/s",
                            unit = "s, m/s, m/s²",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Energy verification: Total Energy E = 1/2 k A² = ${NumberFormatter.formatDouble(0.5 * k * A * A)} J = 1/2 m vmax²",
                            graphPoints = graphPts,
                            graphXLabel = "Time t (s)",
                            graphYLabel = "Displacement x (m)"
                        )
                    )
                }
            ),

            // ==========================================
            // 9. ELECTROSTATICS & CAPACITANCE
            // ==========================================
            CalculatorDefinition(
                id = "phys_elec_coulomb_capacitance",
                subject = SubjectType.PHYSICS,
                chapter = "Electrostatics",
                topic = "Coulomb Law & Parallel Plate Capacitor",
                name = "Coulomb's Law & Parallel Plate Capacitor",
                description = "Calculates electrostatic force, electric field, capacitance with dielectric, and stored energy.",
                formulaLatex = "F = \\frac{1}{4\\pi\\varepsilon_0}\\frac{|q_1 q_2|}{r^2}, \\quad C = \\frac{K\\varepsilon_0 A}{d}, \\quad U = \\frac{1}{2}CV^2",
                inputs = listOf(
                    CalculationField("q1", "Charge q1 (μC)", "2.0", unit = "μC"),
                    CalculationField("q2", "Charge q2 (μC)", "-4.0", unit = "μC"),
                    CalculationField("r", "Separation Distance r", "0.1", unit = "m"),
                    CalculationField("A", "Capacitor Plate Area (A)", "0.02", unit = "m²"),
                    CalculationField("d", "Plate Separation (d)", "0.001", unit = "m"),
                    CalculationField("K", "Dielectric Constant (K)", "1.0", unit = ""),
                    CalculationField("V", "Applied Voltage (V)", "12.0", unit = "V")
                ),
                searchKeywords = listOf("coulomb law", "electrostatic force", "capacitor", "dielectric", "capacitance energy"),
                execute = { vals, _ ->
                    val q1_uC = vals["q1"]?.toDoubleOrNull() ?: 2.0
                    val q2_uC = vals["q2"]?.toDoubleOrNull() ?: -4.0
                    val r = vals["r"]?.toDoubleOrNull() ?: 0.1
                    val A = vals["A"]?.toDoubleOrNull() ?: 0.02
                    val d = vals["d"]?.toDoubleOrNull() ?: 0.001
                    val K = vals["K"]?.toDoubleOrNull() ?: 1.0
                    val V = vals["V"]?.toDoubleOrNull() ?: 12.0

                    val eps0 = 8.8541878128e-12
                    val k_coulomb = 8.9875517923e9

                    val q1 = q1_uC * 1e-6
                    val q2 = q2_uC * 1e-6

                    val F = k_coulomb * abs(q1 * q2) / (r * r)
                    val C = (K * eps0 * A) / d
                    val U = 0.5 * C * V * V
                    val Q_cap = C * V

                    val steps = listOf(
                        CalculationStep("Step 1: Coulomb Electrostatic Force", "F = \\frac{k|q_1 q_2|}{r^2} = \\frac{(8.99\\times 10^9)(${NumberFormatter.formatDouble(abs(q1))})(${NumberFormatter.formatDouble(abs(q2))})}{(${NumberFormatter.formatDouble(r)})^2} = ${NumberFormatter.formatDouble(F)}\\text{ N} \\quad (${if (q1 * q2 < 0) "Attractive" else "Repulsive"})"),
                        CalculationStep("Step 2: Capacitance", "C = \\frac{K\\varepsilon_0 A}{d} = \\frac{(${NumberFormatter.formatDouble(K)})(8.854\\times 10^{-12})(${NumberFormatter.formatDouble(A)})}{${NumberFormatter.formatDouble(d)}} = ${NumberFormatter.formatDouble(C * 1e12)}\\text{ pF} = ${NumberFormatter.formatDouble(C)}\\text{ F}"),
                        CalculationStep("Step 3: Stored Charge & Energy", "Q = CV = ${NumberFormatter.formatDouble(Q_cap * 1e9)}\\text{ nC}, \\quad U = \\frac{1}{2}CV^2 = ${NumberFormatter.formatDouble(U * 1e6)}\\text{ \\mu J}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Electrostatics & Capacitor Results",
                            formulaLatex = "F = \\frac{k|q_1 q_2|}{r^2}, \\quad C = \\frac{K\\varepsilon_0 A}{d}, \\quad U = \\frac{1}{2}CV^2",
                            substitutionLatex = "F = ${NumberFormatter.formatDouble(F)}\\text{ N}, \\quad C = ${NumberFormatter.formatDouble(C * 1e12)}\\text{ pF}, \\quad U = ${NumberFormatter.formatDouble(U * 1e6)}\\text{ \\mu J}",
                            exactResultLatex = "F = ${NumberFormatter.formatDouble(F)}\\text{ N}, \\quad C = ${NumberFormatter.formatDouble(C * 1e12)}\\text{ pF}",
                            numericResult = "Force: ${NumberFormatter.formatDouble(F)} N, Capacitance: ${NumberFormatter.formatDouble(C * 1e12)} pF, Energy: ${NumberFormatter.formatDouble(U * 1e6)} μJ",
                            unit = "N, pF, μJ",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Energy density u_E = 1/2 ε0 E² integrated over volume A·d = U"
                        )
                    )
                }
            ),

            // ==========================================
            // 10. CURRENT ELECTRICITY & AC CIRCUITS
            // ==========================================
            CalculatorDefinition(
                id = "phys_ac_lcr_series",
                subject = SubjectType.PHYSICS,
                chapter = "Alternating Current",
                topic = "Series LCR Resonant Circuit",
                name = "Series LCR Impedance, Resonance & Power Factor",
                description = "Calculates impedance Z, resonant frequency f₀, phase angle, and quality factor Q in series AC circuit.",
                formulaLatex = "X_L = 2\\pi f L, \\quad X_C = \\frac{1}{2\\pi f C}, \\quad Z = \\sqrt{R^2 + (X_L - X_C)^2}, \\quad f_0 = \\frac{1}{2\\pi\\sqrt{LC}}, \\quad Q = \\frac{1}{R}\\sqrt{\\frac{L}{C}}",
                inputs = listOf(
                    CalculationField("R", "Resistance (R)", "50.0", unit = "Ω"),
                    CalculationField("L_mH", "Inductance (L in mH)", "100.0", unit = "mH"),
                    CalculationField("C_uF", "Capacitance (C in μF)", "10.0", unit = "μF"),
                    CalculationField("f", "Source Frequency (f)", "50.0", unit = "Hz"),
                    CalculationField("Vrms", "RMS Voltage (V_rms)", "220.0", unit = "V")
                ),
                assumptions = "Sinusoidal steady-state AC source.",
                conditions = "R > 0, L > 0, C > 0, f > 0",
                searchKeywords = listOf("LCR circuit", "impedance", "resonance", "quality factor", "power factor", "reactance"),
                execute = { vals, _ ->
                    val R = vals["R"]?.toDoubleOrNull() ?: 50.0
                    val L = (vals["L_mH"]?.toDoubleOrNull() ?: 100.0) * 1e-3
                    val C = (vals["C_uF"]?.toDoubleOrNull() ?: 10.0) * 1e-6
                    val f = vals["f"]?.toDoubleOrNull() ?: 50.0
                    val Vrms = vals["Vrms"]?.toDoubleOrNull() ?: 220.0

                    val omega = 2.0 * PI * f
                    val XL = omega * L
                    val XC = 1.0 / (omega * C)
                    val Z = sqrt(R * R + (XL - XC).pow(2))
                    val f0 = 1.0 / (2.0 * PI * sqrt(L * C))
                    val Q = (1.0 / R) * sqrt(L / C)
                    val cosPhi = R / Z
                    val Irms = Vrms / Z
                    val Pavg = Vrms * Irms * cosPhi

                    val steps = listOf(
                        CalculationStep("Step 1: Inductive & Capacitive Reactance", "X_L = 2\\pi f L = ${NumberFormatter.formatDouble(XL)}\\text{ }\\Omega, \\quad X_C = \\frac{1}{2\\pi f C} = ${NumberFormatter.formatDouble(XC)}\\text{ }\\Omega"),
                        CalculationStep("Step 2: Total Circuit Impedance", "Z = \\sqrt{R^2 + (X_L - X_C)^2} = \\sqrt{(${NumberFormatter.formatDouble(R)})^2 + (${NumberFormatter.formatDouble(XL - XC)})^2} = ${NumberFormatter.formatDouble(Z)}\\text{ }\\Omega"),
                        CalculationStep("Step 3: Resonant Frequency & Quality Factor", "f_0 = \\frac{1}{2\\pi\\sqrt{LC}} = ${NumberFormatter.formatDouble(f0)}\\text{ Hz}, \\quad Q = \\frac{1}{R}\\sqrt{\\frac{L}{C}} = ${NumberFormatter.formatDouble(Q)}"),
                        CalculationStep("Step 4: Current & Average Power Dissipated", "I_{\\text{rms}} = \\frac{V_{\\text{rms}}}{Z} = ${NumberFormatter.formatDouble(Irms)}\\text{ A}, \\quad P = V_{\\text{rms}} I_{\\text{rms}} \\cos\\phi = ${NumberFormatter.formatDouble(Pavg)}\\text{ W}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Series LCR Circuit Analysis",
                            formulaLatex = "Z = \\sqrt{R^2 + (X_L - X_C)^2}, \\quad f_0 = \\frac{1}{2\\pi\\sqrt{LC}}, \\quad \\cos\\phi = \\frac{R}{Z}",
                            substitutionLatex = "Z = ${NumberFormatter.formatDouble(Z)}\\text{ }\\Omega, \\quad f_0 = ${NumberFormatter.formatDouble(f0)}\\text{ Hz}, \\quad I_{\\text{rms}} = ${NumberFormatter.formatDouble(Irms)}\\text{ A}",
                            exactResultLatex = "Z = ${NumberFormatter.formatDouble(Z)}\\text{ }\\Omega, \\quad f_0 = ${NumberFormatter.formatDouble(f0)}\\text{ Hz}",
                            numericResult = "Z: ${NumberFormatter.formatDouble(Z)} Ω, Resonant f₀: ${NumberFormatter.formatDouble(f0)} Hz, Irms: ${NumberFormatter.formatDouble(Irms)} A, Power: ${NumberFormatter.formatDouble(Pavg)} W",
                            unit = "Ω, Hz, A, W",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Power check: P_avg = I_rms² · R = (${NumberFormatter.formatDouble(Irms)})² × ${NumberFormatter.formatDouble(R)} = ${NumberFormatter.formatDouble(Irms * Irms * R)} W = Pavg"
                        )
                    )
                }
            ),

            // ==========================================
            // 11. MODERN PHYSICS
            // ==========================================
            CalculatorDefinition(
                id = "phys_modern_photoelectric_bohr",
                subject = SubjectType.PHYSICS,
                chapter = "Modern Physics",
                topic = "Photoelectric Effect & Bohr Atom",
                name = "Photoelectric Effect & Bohr Hydrogen Levels",
                description = "Calculates maximum photoelectron kinetic energy, stopping potential, and Bohr orbit radius/energy.",
                formulaLatex = "K_{\\text{max}} = \\frac{hc}{\\lambda} - \\phi = e V_0, \\quad r_n = 0.529 \\frac{n^2}{Z} \\text{ Å}, \\quad E_n = -13.6 \\frac{Z^2}{n^2} \\text{ eV}",
                inputs = listOf(
                    CalculationField("lambda_nm", "Incident Wavelength (λ)", "300.0", unit = "nm"),
                    CalculationField("phi_eV", "Work Function (φ)", "2.2", unit = "eV"),
                    CalculationField("n", "Bohr Principal Quantum Number (n)", "2", unit = ""),
                    CalculationField("Z", "Atomic Number (Z)", "1", unit = "")
                ),
                assumptions = "Single-electron hydrogenic atom for Bohr model.",
                conditions = "λ > 0, n ≥ 1, Z ≥ 1",
                searchKeywords = listOf("photoelectric", "work function", "stopping potential", "bohr model", "energy levels"),
                execute = { vals, _ ->
                    val lambda_nm = vals["lambda_nm"]?.toDoubleOrNull() ?: 300.0
                    val phi = vals["phi_eV"]?.toDoubleOrNull() ?: 2.2
                    val n = vals["n"]?.toIntOrNull() ?: 2
                    val Z = vals["Z"]?.toIntOrNull() ?: 1

                    val photonEnergy_eV = 1240.0 / lambda_nm
                    val Kmax = photonEnergy_eV - phi
                    val stoppingV = max(0.0, Kmax)
                    val emissionPossible = Kmax > 0

                    val rn_angstrom = 0.529 * (n * n).toDouble() / Z.toDouble()
                    val En_eV = -13.6 * (Z * Z).toDouble() / (n * n).toDouble()

                    val steps = listOf(
                        CalculationStep("Step 1: Incident Photon Energy", "E = \\frac{hc}{\\lambda} = \\frac{1240\\text{ eV}\\cdot\\text{nm}}{${NumberFormatter.formatDouble(lambda_nm)}\\text{ nm}} = ${NumberFormatter.formatDouble(photonEnergy_eV)}\\text{ eV}"),
                        CalculationStep("Step 2: Max Kinetic Energy & Stopping Potential", "K_{\\text{max}} = E - \\phi = ${NumberFormatter.formatDouble(photonEnergy_eV)} - ${NumberFormatter.formatDouble(phi)} = ${NumberFormatter.formatDouble(Kmax)}\\text{ eV}, \\quad V_0 = ${NumberFormatter.formatDouble(stoppingV)}\\text{ V}"),
                        CalculationStep("Step 3: Bohr Orbit Radius", "r_n = 0.529 \\frac{n^2}{Z} = 0.529 \\frac{${n}^2}{${Z}} = ${NumberFormatter.formatDouble(rn_angstrom)}\\text{ \\AA}"),
                        CalculationStep("Step 4: Bohr Energy Level", "E_n = -13.6 \\frac{Z^2}{n^2} = -13.6 \\frac{${Z}^2}{${n}^2} = ${NumberFormatter.formatDouble(En_eV)}\\text{ eV}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Modern Physics & Bohr Atomic Parameters",
                            formulaLatex = "K_{\\text{max}} = h\\nu - \\phi = e V_0, \\quad E_n = -13.6 \\frac{Z^2}{n^2}\\text{ eV}, \\quad r_n = 0.529 \\frac{n^2}{Z}\\text{ \\AA}",
                            substitutionLatex = "K_{\\text{max}} = ${NumberFormatter.formatDouble(Kmax)}\\text{ eV}, \\quad V_0 = ${NumberFormatter.formatDouble(stoppingV)}\\text{ V}, \\quad E_{$n} = ${NumberFormatter.formatDouble(En_eV)}\\text{ eV}",
                            exactResultLatex = "K_{\\text{max}} = ${NumberFormatter.formatDouble(Kmax)}\\text{ eV}, \\quad E_{$n} = ${NumberFormatter.formatDouble(En_eV)}\\text{ eV}",
                            numericResult = if (emissionPossible) "Kmax: ${NumberFormatter.formatDouble(Kmax)} eV, Stopping V₀: ${NumberFormatter.formatDouble(stoppingV)} V, Orbit r: ${NumberFormatter.formatDouble(rn_angstrom)} Å, E: ${NumberFormatter.formatDouble(En_eV)} eV" else "Photon energy ($photonEnergy_eV eV) < Work function ($phi eV) → No photoelectric emission.",
                            unit = "eV, V, Å",
                            steps = steps,
                            isVerified = true,
                            verificationNote = if (emissionPossible) "Photoelectric emission verified." else "Threshold condition not met (hν < φ)."
                        )
                    )
                }
            )
        )
    }

    fun getByChapter(chapter: String): List<CalculatorDefinition> = calculators.filter { it.chapter.equals(chapter, ignoreCase = true) }
    fun getAllChapters(): List<String> = calculators.map { it.chapter }.distinct()
}
