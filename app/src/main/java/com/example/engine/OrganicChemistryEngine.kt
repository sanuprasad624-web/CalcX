package com.example.engine

import kotlin.math.*

data class OrganicReaction(
    val reactant: String,
    val reagent: String,
    val majorProduct: String,
    val minorProduct: String = "",
    val reactionName: String,
    val mechanismType: String,
    val regiochemistry: String,
    val stereochemistry: String,
    val reactionEquationLatex: String,
    val steps: List<String>,
    val jeeNotes: String
)

data class MechanismDetail(
    val name: String,
    val category: String,
    val substrate: String,
    val reagent: String,
    val rateLaw: String,
    val intermediate: String,
    val stereochemistry: String,
    val solventEffect: String,
    val steps: List<String>,
    val jeeKeyPoints: List<String>
)

data class GocConcept(
    val title: String,
    val category: String,
    val summary: String,
    val orderSeriesLatex: String,
    val rules: List<String>,
    val jeeExceptions: List<String>
)

data class IsomerismConcept(
    val title: String,
    val type: String, // "Structural" or "Stereoisomerism"
    val definition: String,
    val formulaLatex: String,
    val conditions: List<String>,
    val examples: List<String>
)

data class NamedOrganicReaction(
    val name: String,
    val reactant: String,
    val reagents: String,
    val product: String,
    val equationLatex: String,
    val keyIntermediates: String,
    val importantNotes: String
)

data class FunctionalGroupTest(
    val functionalGroup: String,
    val testName: String,
    val reagentUsed: String,
    val positiveObservation: String,
    val equationLatex: String,
    val notes: String
)

data class ReagentSelectivity(
    val reagent: String,
    val role: String,
    val functionalGroupAction: String,
    val exceptionsAndSelectivity: String
)

data class OrganicCalcResult(
    val title: String,
    val formulaLatex: String,
    val steps: List<String>,
    val finalAnswer: String
)

object OrganicChemistryEngine {

    val REACTIONS: List<OrganicReaction> = listOf(
        OrganicReaction(
            reactant = "Propene (CH₃-CH=CH₂)",
            reagent = "HBr",
            majorProduct = "2-Bromopropane (CH₃-CH(Br)-CH₃)",
            minorProduct = "1-Bromopropane",
            reactionName = "Electrophilic Addition of Hydrogen Halide",
            mechanismType = "Electrophilic Addition via Carbocation",
            regiochemistry = "Markovnikov's Rule (H⁺ adds to carbon with more hydrogens to form stable 2° carbocation)",
            stereochemistry = "Achiral / Racemic planar attack",
            reactionEquationLatex = "\\text{CH}_3-\\text{CH}=\\text{CH}_2 + \\text{HBr} \\rightarrow \\text{CH}_3-\\text{CH(Br)}-\\text{CH}_3",
            steps = listOf(
                "Step 1: Protonation of alkene double bond by H⁺ yields stable 2° carbocation: \\text{CH}_3-\\text{C}^+\\text{H}-\\text{CH}_3 (more stable than 1° due to 6 hyperconjugative α-H).",
                "Step 2: Nucleophilic attack of Br⁻ on the planar 2° carbocation gives 2-bromopropane as major product."
            ),
            jeeNotes = "TRAP: Carbocation rearrangements (hydride or methyl shift) can occur if a more stable 3° carbocation can be formed! E.g. 3,3-dimethyl-1-butene gives 2-bromo-2,3-dimethylbutane."
        ),
        OrganicReaction(
            reactant = "Propene (CH₃-CH=CH₂)",
            reagent = "HBr + Peroxide (R-O-O-R, hν)",
            majorProduct = "1-Bromopropane (CH₃-CH₂-CH₂-Br)",
            minorProduct = "2-Bromopropane",
            reactionName = "Peroxide Effect / Kharasch Effect",
            mechanismType = "Free Radical Addition",
            regiochemistry = "Anti-Markovnikov's Rule (Br• adds to terminal carbon to form more stable 2° carbon radical)",
            stereochemistry = "Radical intermediate",
            reactionEquationLatex = "\\text{CH}_3-\\text{CH}=\\text{CH}_2 + \\text{HBr} \\xrightarrow{\\text{Peroxide, } h\\nu} \\text{CH}_3-\\text{CH}_2-\\text{CH}_2\\text{Br}",
            steps = listOf(
                "Initiation: R-O-O-R homolysis gives 2 RO•; RO• + HBr → ROH + Br•.",
                "Propagation: Br• attacks CH₂=CH-CH₃ forming stable 2° radical \\text{CH}_3-\\dot{\\text{C}}\\text{H}-\\text{CH}_2\\text{Br}.",
                "Radical abstracts H• from HBr regenerating Br• and yielding 1-bromopropane."
            ),
            jeeNotes = "CRITICAL JEE FACT: Peroxide effect is ONLY shown by HBr. HF and HCl bonds are too strong (step 2 endothermic); for HI, I-I bond formation is preferred over alkene addition (step 1 endothermic)."
        ),
        OrganicReaction(
            reactant = "2-Bromobutane (CH₃-CH(Br)-CH₂-CH₃)",
            reagent = "Alcoholic KOH + Heat (alc. KOH, Δ)",
            majorProduct = "2-Butene (CH₃-CH=CH-CH₃) [Saytzeff / Zaitsev]",
            minorProduct = "1-Butene (CH₂=CH-CH₂-CH₃) [Hofmann]",
            reactionName = "Dehydrohalogenation (β-elimination)",
            mechanismType = "E2 (Concerted Bimolecular Elimination)",
            regiochemistry = "Saytzeff (Zaitsev) Rule: More substituted, hyperconjugated alkene is major (trans > cis)",
            stereochemistry = "Anti-coplanar transition state (H and Br must be 180° periplanar)",
            reactionEquationLatex = "\\text{CH}_3\\text{CH(Br)CH}_2\\text{CH}_3 \\xrightarrow{\\text{alc. KOH, } \\Delta} \\text{CH}_3\\text{CH}=\\text{CHCH}_3 \\text{ (Major 80\\%)} + \\text{CH}_2=\\text{CHCH}_2\\text{CH}_3",
            steps = listOf(
                "Concerted single-step E2 mechanism: Base (OH⁻) attacks β-hydrogen anti-periplanar to leaving group Br⁻.",
                "Double bond forms simultaneously as C-Br bond breaks."
            ),
            jeeNotes = "TRAP: If a bulky base like Potassium tert-butoxide (t-BuO⁻K⁺) is used, steric hindrance forces removal of less hindered 1° β-hydrogen, making Hofmann product (1-butene) major!"
        ),
        OrganicReaction(
            reactant = "2-Bromobutane",
            reagent = "Potassium tert-butoxide (t-BuOK, Δ)",
            majorProduct = "1-Butene (CH₂=CH-CH₂-CH₃) [Hofmann Product]",
            minorProduct = "2-Butene",
            reactionName = "Sterically Hindered E2 Elimination",
            mechanismType = "E2 Elimination",
            regiochemistry = "Hofmann Rule (least substituted alkene formed due to steric hindrance)",
            stereochemistry = "Anti-periplanar",
            reactionEquationLatex = "\\text{CH}_3\\text{CH(Br)CH}_2\\text{CH}_3 \\xrightarrow{(CH_3)_3CO^-K^+} \\text{CH}_2=\\text{CH-CH}_2-\\text{CH}_3",
            steps = listOf(
                "Bulky t-butoxide anion cannot access the internal 2° β-hydrogen easily.",
                "Deprotonates the sterically accessible terminal 1° β-hydrogen rapidly.",
                "Generates 1-butene as the predominant Hofmann product."
            ),
            jeeNotes = "Bulky bases (t-BuOK, LDA) and poor leaving groups (-F, -N⁺R₃, -S⁺R₂) consistently give Hofmann alkene."
        ),
        OrganicReaction(
            reactant = "Benzene (C₆H₆)",
            reagent = "CH₃Cl + Anhydrous AlCl₃ (Friedel-Crafts)",
            majorProduct = "Toluene (C₆H₅-CH₃)",
            minorProduct = "Polyalkylated benzenes",
            reactionName = "Friedel-Crafts Alkylation",
            mechanismType = "Electrophilic Aromatic Substitution (EAS)",
            regiochemistry = "Mono-substitution on benzene ring",
            stereochemistry = "Planar arenium intermediate",
            reactionEquationLatex = "\\text{C}_6\\text{H}_6 + \\text{CH}_3\\text{Cl} \\xrightarrow{\\text{anh. AlCl}_3} \\text{C}_6\\text{H}_5-\\text{CH}_3 + \\text{HCl}",
            steps = listOf(
                "Generation of electrophile: \\text{CH}_3\\text{Cl} + \\text{AlCl}_3 \\rightarrow \\text{CH}_3^+ + \\text{AlCl}_4^-.",
                "Attack of benzene π-cloud on \\text{CH}_3^+ forms resonance-stabilized arenium ion (σ-complex / Wheland intermediate).",
                "Loss of proton restores aromaticity, regenerating AlCl₃ catalyst."
            ),
            jeeNotes = "LIMITATIONS: 1. Polyalkylation occurs because alkyl group activates ring. 2. Rearrangement occurs for 1-chloropropane giving isopropylbenzene (cumene). 3. Fails with strongly deactivating groups (-NO₂, -COOH) or aniline (-NH₂ forms complex with Lewis acid AlCl₃)."
        ),
        OrganicReaction(
            reactant = "Acetaldehyde (CH₃CHO)",
            reagent = "Dilute NaOH (room temp)",
            majorProduct = "3-Hydroxybutanal (Aldol) -> 2-Butenal (Crotonaldehyde on heating)",
            minorProduct = "Self-condensation polymers",
            reactionName = "Aldol Condensation",
            mechanismType = "Nucleophilic Addition to Carbonyl via Enolate",
            regiochemistry = "Addition at α-carbon",
            stereochemistry = "Trans-enone favored on dehydration",
            reactionEquationLatex = "2\\,\\text{CH}_3\\text{CHO} \\xrightarrow{\\text{dil. NaOH}} \\text{CH}_3\\text{CH(OH)CH}_2\\text{CHO} \\xrightarrow{\\Delta, -\\text{H}_2\\text{O}} \\text{CH}_3\\text{CH}=\\text{CH-CHO}",
            steps = listOf(
                "Step 1: OH⁻ abstracts α-hydrogen forming resonance-stabilized enolate: [\\text{CH}_2=\\text{CH}-\\text{O}^- \\leftrightarrow \\bar{\\text{C}}\\text{H}_2-\\text{CH}=\\text{O}].",
                "Step 2: Enolate attacks carbonyl carbon of another acetaldehyde molecule.",
                "Step 3: Protonation yields β-hydroxy aldehyde (aldol).",
                "Step 4: Dehydration on heating via E1cb mechanism gives conjugated α,β-unsaturated aldehyde (crotonaldehyde)."
            ),
            jeeNotes = "Aldehydes/ketones must have at least one α-hydrogen to undergo aldol. If no α-H (e.g. HCHO, C₆H₅CHO), Cannizzaro reaction occurs instead!"
        ),
        OrganicReaction(
            reactant = "Benzaldehyde (C₆H₅CHO)",
            reagent = "Concentrated 50% NaOH (Δ)",
            majorProduct = "Benzyl Alcohol (C₆H₅CH₂OH) + Sodium Benzoate (C₆H₅COONa)",
            minorProduct = "None (Disproportionation)",
            reactionName = "Cannizzaro Reaction",
            mechanismType = "Nucleophilic Addition & Hydride Transfer Disproportionation",
            regiochemistry = "Redox disproportionation (Self oxidation-reduction)",
            stereochemistry = "N/A",
            reactionEquationLatex = "2\\,\\text{C}_6\\text{H}_5\\text{CHO} + \\text{NaOH} \\rightarrow \\text{C}_6\\text{H}_5\\text{CH}_2\\text{OH} + \\text{C}_6\\text{H}_5\\text{COONa}",
            steps = listOf(
                "Step 1: OH⁻ attacks carbonyl carbon of one benzaldehyde molecule forming a dianion/monoanion intermediate.",
                "Step 2: Rate-determining step (RDS) is intramolecular/intermolecular hydride ion (:H⁻) transfer to second benzaldehyde carbonyl.",
                "Step 3: Rapid proton exchange produces carboxylate anion and primary alcohol."
            ),
            jeeNotes = "In Cross-Cannizzaro of HCHO with C₆H₅CHO, Formaldehyde (HCHO) is ALWAYS oxidized to formic acid (HCOONa) because it is sterically less hindered and more electrophilic; C₆H₅CHO is reduced to benzyl alcohol."
        ),
        OrganicReaction(
            reactant = "Phenol (C₆H₅OH)",
            reagent = "CHCl₃ + Aqueous NaOH (Δ)",
            majorProduct = "Salicylaldehyde (2-Hydroxybenzaldehyde)",
            minorProduct = "p-Hydroxybenzaldehyde",
            reactionName = "Reimer-Tiemann Reaction",
            mechanismType = "Electrophilic Aromatic Substitution via Dichlorocarbene",
            regiochemistry = "Ortho-selective due to intra-molecular H-bonding in product and intermediate chelation",
            stereochemistry = "Planar ring substitution",
            reactionEquationLatex = "\\text{C}_6\\text{H}_5\\text{OH} + \\text{CHCl}_3 + 3\\,\\text{NaOH} \\rightarrow o-\\text{C}_6\\text{H}_4(\\text{OH})(\\text{CHO}) + 3\\,\\text{NaCl} + 2\\,\\text{H}_2\\text{O}",
            steps = listOf(
                "Generation of electrophile: OH⁻ removes proton from CHCl₃ giving :CCl₃⁻, which eliminates Cl⁻ to form neutral singlet Dichlorocarbene (:CCl₂).",
                "Phenoxide ion attacks the electrophilic dichlorocarbene at ortho position.",
                "Hydrolysis of gem-dichloride intermediate yields ortho-hydroxybenzaldehyde (salicylaldehyde)."
            ),
            jeeNotes = "Electrophile is neutral Dichlorocarbene (:CCl₂), which has an incomplete octet (6 electrons). If CCl₄ is used instead of CHCl₃, Salicylic acid is formed!"
        ),
        OrganicReaction(
            reactant = "Phenol (C₆H₅OH)",
            reagent = "1. NaOH, 2. CO₂ (4-7 atm, 400 K), 3. H⁺",
            majorProduct = "Salicylic Acid (2-Hydroxybenzoic acid)",
            minorProduct = "4-Hydroxybenzoic acid",
            reactionName = "Kolbe-Schmitt Reaction",
            mechanismType = "Electrophilic Aromatic Substitution by weak electrophile CO₂",
            regiochemistry = "Ortho major due to sodium phenoxide chelate transition state",
            stereochemistry = "Planar",
            reactionEquationLatex = "\\text{C}_6\\text{H}_5\\text{OH} \\xrightarrow{1.\\,\\text{NaOH},\\, 2.\\,\\text{CO}_2,\\, 3.\\,\\text{H}^+} o-\\text{C}_6\\text{H}_4(\\text{OH})(\\text{COOH})",
            steps = listOf(
                "Phenol is converted into highly nucleophilic Sodium Phenoxide by NaOH.",
                "Phenoxide undergoes EAS attack on weak electrophile carbon dioxide (CO₂).",
                "Acidification yields salicylic acid, the precursor to Aspirin (acetylsalicylic acid)."
            ),
            jeeNotes = "Aspirin synthesis: Salicylic acid + Acetic anhydride (in presence of H⁺) → Acetylsalicylic acid (Aspirin) + CH₃COOH."
        ),
        OrganicReaction(
            reactant = "Acetamide (CH₃CONH₂)",
            reagent = "Br₂ + 4 KOH (Δ)",
            majorProduct = "Methylamine (CH₃NH₂)",
            minorProduct = "K₂CO₃ + 2 KBr + 2 H₂O",
            reactionName = "Hoffmann Bromamide Degradation",
            mechanismType = "Intramolecular Rearrangement via Isocyanate intermediate",
            regiochemistry = "One carbon degradation (loss of carbonyl carbon as carbonate)",
            stereochemistry = "Retention of configuration at migrating alkyl group",
            reactionEquationLatex = "\\text{CH}_3\\text{CONH}_2 + \\text{Br}_2 + 4\\,\\text{KOH} \\rightarrow \\text{CH}_3\\text{NH}_2 + \\text{K}_2\\text{CO}_3 + 2\\,\\text{KBr} + 2\\,\\text{H}_2\\text{O}",
            steps = listOf(
                "1. N-Bromination gives N-bromoacetamide: CH₃CONHBr.",
                "2. Base abstracts N-H proton forming bromamide anion, which undergoes loss of Br⁻ and concerted 1,2-migration of CH₃ group to electron-deficient N (Nitrene precursor).",
                "3. Generates Methyl Isocyanate (CH₃-N=C=O).",
                "4. Alkaline hydrolysis of isocyanate yields 1° amine (CH₃NH₂) and carbonate."
            ),
            jeeNotes = "Used to step-down homologous series by one carbon! Migrating group retains its stereochemical configuration. 1 mole amide requires 1 mole Br₂ and 4 moles KOH."
        ),
        OrganicReaction(
            reactant = "Acetone (CH₃COCH₃)",
            reagent = "I₂ + NaOH (or NaOI, Δ)",
            majorProduct = "Iodoform (CHI₃, Yellow crystalline precipitate) + Sodium Acetate",
            minorProduct = "NaI + H₂O",
            reactionName = "Iodoform Reaction (Haloform Test)",
            mechanismType = "Successive α-Halogenation followed by Nucleophilic Acyl Cleavage",
            regiochemistry = "Specific for compounds containing CH₃-C=O or CH₃-CH(OH)- group",
            stereochemistry = "Cleavage",
            reactionEquationLatex = "\\text{CH}_3\\text{COCH}_3 + 3\\,\\text{I}_2 + 4\\,\\text{NaOH} \\rightarrow \\text{CHI}_3\\downarrow (\\text{yellow}) + \\text{CH}_3\\text{COONa} + 3\\,\\text{NaI} + 3\\,\\text{H}_2\\text{O}",
            steps = listOf(
                "Base abstracts α-hydrogens of methyl ketone successively, halogenating with I₂ to form tri-iodomethyl ketone: \\text{CH}_3-\\text{CO}-\\text{CI}_3.",
                "OH⁻ attacks carbonyl carbon; :CI₃⁻ acts as a good leaving group due to resonance and inductive stabilization by 3 iodine atoms.",
                "Proton transfer gives iodoform (CHI₃, MP 119°C) yellow solid."
            ),
            jeeNotes = "Gives positive test with: Ethanol (CH₃CH₂OH), Acetaldehyde (CH₃CHO), Methyl ketones (CH₃COR), and 2-Alkanols (CH₃CH(OH)R). Methanol, 1-propanol, and 3-pentanone give NEGATIVE test."
        )
    )

    val MECHANISMS: List<MechanismDetail> = listOf(
        MechanismDetail(
            name = "S_N1 (Substitution Nucleophilic Unimolecular)",
            category = "Nucleophilic Substitution",
            substrate = "3° Alkyl Halides > Benzyl / Allyl > 2° (1° rarely)",
            reagent = "Weak nucleophile (H₂O, ROH), Polar Protic Solvent",
            rateLaw = "\\text{Rate} = k[\\text{R-X}] \\text{ (First order, independent of [Nu])}",
            intermediate = "Planar Carbocation (sp² hybridized)",
            stereochemistry = "Racemization with partial inversion (inversion > retention due to ion pair effect)",
            solventEffect = "Favored by polar protic solvents (H₂O, EtOH) which stabilize both carbocation and halide leaving group",
            steps = listOf(
                "Step 1 (RDS - Slow): Heterolytic cleavage of C-X bond forming planar carbocation: R_3C-X \\rightarrow R_3C^+ + X^-.",
                "Optional: Carbocation rearrangement (1,2-hydride or methyl shift or ring expansion).",
                "Step 2 (Fast): Nucleophile attacks planar carbocation from front or back with equal probability.",
                "Step 3: Deprotonation if neutral nucleophile was used."
            ),
            jeeKeyPoints = listOf(
                "Reactivity order: 3° > 2° > 1° > CH₃X.",
                "Allylic and benzylic halides react very rapidly due to resonance-stabilized carbocations.",
                "Rearrangement is common! Always check for ring expansion (4->5, 5->6) or hydride/methyl shift."
            )
        ),
        MechanismDetail(
            name = "S_N2 (Substitution Nucleophilic Bimolecular)",
            category = "Nucleophilic Substitution",
            substrate = "CH₃X > 1° > 2° (3° does NOT react due to steric hindrance)",
            reagent = "Strong, non-bulky nucleophile (CN⁻, I⁻, OH⁻, N₃⁻), Polar Aprotic Solvent",
            rateLaw = "\\text{Rate} = k[\\text{R-X}][\\text{Nu}^-] \\text{ (Second order)}",
            intermediate = "No intermediate; 5-coordinate trigonal bipyramidal Transition State",
            stereochemistry = "Complete 100% Walden Inversion (Umbrella inversion)",
            solventEffect = "Favored by polar aprotic solvents (DMSO, DMF, Acetone, Acetonitrile) which dissolve cations, leaving nucleophile anions 'naked' and highly reactive",
            steps = listOf(
                "Single concerted step: Nucleophile attacks carbon from exactly 180° opposite the leaving group (backside attack).",
                "Transition state has partially formed C-Nu bond and partially broken C-X bond with partial negative charges.",
                "Leaving group departs, resulting in complete inversion of configuration at chiral carbon."
            ),
            jeeKeyPoints = listOf(
                "Reactivity order: CH₃X > 1° > 2° >> 3° (essentially 0).",
                "Neopentyl halide ((CH₃)₃C-CH₂X) reacts extremely slowly due to severe β-steric hindrance.",
                "Leaving group ability: I⁻ > Br⁻ > Cl⁻ >> F⁻."
            )
        ),
        MechanismDetail(
            name = "E1 (Elimination Unimolecular)",
            category = "Elimination Reactions",
            substrate = "3° > 2° Alkyl Halides / Alcohols (acid catalyzed)",
            reagent = "Weak base, Heat (Δ), Polar protic solvent",
            rateLaw = "\\text{Rate} = k[\\text{Substrate}]",
            intermediate = "Carbocation intermediate (competes directly with S_N1)",
            stereochemistry = "Saytzeff (more stable E-alkene / trans isomer favored)",
            solventEffect = "Favored by polar protic solvents with high temperature",
            steps = listOf(
                "Step 1 (RDS): Cleavage of C-X gives carbocation.",
                "Optional: Rearrangement to more stable carbocation.",
                "Step 2: Weak base removes β-proton from carbocation forming alkene."
            ),
            jeeKeyPoints = listOf(
                "High temperature (heat) always favors elimination over substitution (entropy ΔS > 0).",
                "Dehydration of alcohols with conc. H₂SO₄ at 170°C proceeds via E1 mechanism."
            )
        ),
        MechanismDetail(
            name = "E2 (Elimination Bimolecular)",
            category = "Elimination Reactions",
            substrate = "3° > 2° > 1° (all react, 3° most reactive towards elimination)",
            reagent = "Strong base (alc. KOH, NaNH₂, RO⁻, t-BuO⁻) + Heat",
            rateLaw = "\\text{Rate} = k[\\text{R-X}][\\text{Base}]",
            intermediate = "No intermediate; concerted transition state",
            stereochemistry = "Anti-periplanar geometry required (dihedral angle 180° between β-H and leaving group)",
            solventEffect = "Alcoholic solvent (EtOH) at elevated temperature",
            steps = listOf(
                "Single concerted step: Base extracts β-hydrogen while electrons form double bond and leaving group departs simultaneously.",
                "Requires anti-coplanar conformation of C-H and C-X bonds."
            ),
            jeeKeyPoints = listOf(
                "Saytzeff alkene is major with small bases (OH⁻, OEt⁻).",
                "Hofmann alkene is major with bulky bases (t-BuOK) or poor leaving groups (F⁻, N⁺R₃).",
                "In cyclohexanes, both H and X must be trans-diaxial to eliminate!"
            )
        ),
        MechanismDetail(
            name = "Electrophilic Aromatic Substitution (EAS)",
            category = "Aromatic Chemistry",
            substrate = "Benzene and substituted derivatives",
            reagent = "Electrophile generation: Nitration (HNO₃/H₂SO₄), Halogenation (X₂/FeX₃), Sulphonation (fuming H₂SO₄), Friedel-Crafts",
            rateLaw = "\\text{Rate} = k[\\text{Ar-H}][\\text{E}^+]",
            intermediate = "Arenium ion / Sigma complex (Wheland intermediate)",
            stereochemistry = "Planar aromatic ring preserved",
            solventEffect = "Lewis acid catalysts in non-aqueous solvents",
            steps = listOf(
                "Step 1: Generation of reactive electrophile (NO₂⁺, Br⁺, R⁺, RCO⁺, SO₃).",
                "Step 2 (RDS): Electrophile attacks π-electron cloud forming resonance-stabilized carbocation (sigma complex).",
                "Step 3 (Fast): Base removes proton to re-aromatize ring with large resonance stabilization energy."
            ),
            jeeKeyPoints = listOf(
                "Activating groups (+M, +I like -OH, -NH₂, -CH₃) are ortho/para directing.",
                "Deactivating groups (-M, -I like -NO₂, -CN, -COOH) are meta directing.",
                "Halogens (-Cl, -Br) are DEACTIVATING due to strong -I, but ORTHO/PARA directing due to +M resonance!"
            )
        )
    )

    val GOC_CONCEPTS: List<GocConcept> = listOf(
        GocConcept(
            title = "Inductive Effect (I-Effect)",
            category = "Electronic Effects",
            summary = "Permanent polarization of σ-bonds due to electronegativity differences. Transmitted through σ-electrons, decreases rapidly with distance (negligible beyond 3 carbons).",
            orderSeriesLatex = "\\text{-I Series: } -\\text{N}^+\\text{R}_3 > -\\text{NO}_2 > -\\text{CN} > -\\text{COOH} > -\\text{F} > -\\text{Cl} > -\\text{Br} > -\\text{I} > -\\text{OR} > -\\text{OH} > -\\text{C}_6\\text{H}_5 > -\\text{H}\\\\\n\\text{+I Series: } -\\text{O}^- > -\\text{COO}^- > -\\text{C(CH}_3)_3 (3^\\circ) > -\\text{CH(CH}_3)_2 (2^\\circ) > -\\text{CH}_2\\text{CH}_3 (1^\\circ) > -\\text{CH}_3 > -\\text{T} > -\\text{D} > -\\text{H}",
            rules = listOf(
                "Electron withdrawing (-I) groups stabilize carbanions and increase acidity of carboxylic acids.",
                "Electron donating (+I) groups stabilize carbocations and free radicals, but decrease acidity of acids."
            ),
            jeeExceptions = listOf(
                "Deuterium (+I effect of -D > -H) due to smaller C-D bond length and greater electron density.",
                "Tritium (+I: -T > -D > -H)."
            )
        ),
        GocConcept(
            title = "Resonance & Mesomeric Effect (M-Effect)",
            category = "Electronic Effects",
            summary = "Delocalization of π-electrons or lone pairs through conjugated systems. Operates equally at ortho and para positions, but does NOT operate at meta position.",
            orderSeriesLatex = "\\text{+M Series: } -\\text{O}^- > -\\text{NH}_2 > -\\text{NHR} > -\\text{OH} > -\\text{OR} > -\\text{NHCOR} > -\\text{OCOR} > -\\text{Ph} > -\\text{F} > -\\text{Cl} > -\\text{Br} > -\\text{I}\\\\\n\\text{-M Series: } -\\text{NO}_2 > -\\text{CN} > -\\text{SO}_3\\text{H} > -\\text{CHO} > -\\text{COR} > -\\text{COOH} > -\\text{COOR} > -\\text{CONH}_2",
            rules = listOf(
                "Resonance energy is highest when equivalent resonance structures exist (e.g. acetate ion CH₃COO⁻ > phenoxide).",
                "Structure with complete octet for all atoms is more stable, even if formal charge is on electronegative atom."
            ),
            jeeExceptions = listOf(
                "Halobenzenes: -I effect dominates over +M effect for overall reactivity (deactivating), but +M determines orientation (ortho/para directing).",
                "SIR (Steric Inhibition of Resonance): Bulky groups at ortho position force nitro or amino group out of aromatic plane, inhibiting resonance."
            )
        ),
        GocConcept(
            title = "Aromaticity (Hückel's Rule)",
            category = "Aromaticity",
            summary = "A compound is Aromatic if it is cyclic, completely conjugated, planar, and has (4n + 2) π-electrons (n = 0, 1, 2...).",
            orderSeriesLatex = "\\text{Stability: } \\text{Aromatic} \\gg \\text{Non-Aromatic} > \\text{Anti-Aromatic}",
            rules = listOf(
                "Aromatic: Cyclic, planar, completely conjugated, (4n + 2) π electrons (2, 6, 10, 14...). Extra resonance stabilization.",
                "Anti-Aromatic: Cyclic, planar, completely conjugated, 4n π electrons (4, 8, 12...). Exceptionally unstable.",
                "Non-Aromatic: Any compound that violates cyclic, planarity, or conjugation conditions (e.g., Cyclooctatetraene is tub-shaped non-aromatic to avoid anti-aromaticity)."
            ),
            jeeExceptions = listOf(
                "Cyclopentadienyl anion has 6 π electrons (aromatic, highly stable pKa ~ 16).",
                "Cycloheptatrienyl cation (Tropylium ion) has 6 π electrons (aromatic, remarkably stable).",
                "Cyclobutadiene (4 π electrons) is anti-aromatic and dimerizes instantly at room temp."
            )
        ),
        GocConcept(
            title = "Carbocation Stability & Rearrangement",
            category = "Reaction Intermediates",
            summary = "Carbocations are electron-deficient (6 valence electrons, sp² planar). Stabilized by resonance, hyperconjugation, and +I.",
            orderSeriesLatex = "\\text{Stability: } \\text{Tropylium} > \\text{Cyclopropylmethyl (CPM)} > \\text{Triphenylmethyl} > 3^\\circ \\text{ Allyl} \\approx 3^\\circ \\text{ Benzyl} > 3^\\circ > 2^\\circ > 1^\\circ > \\text{CH}_3^+",
            rules = listOf(
                "Hyperconjugation: Stability directly proportional to number of α-hydrogens.",
                "Rearrangement propensity: 1,2-Hydride shift > 1,2-Methide shift > 1,2-Phenyl shift.",
                "Ring expansion: 4-membered ring with adjacent carbocation expands to 5; 5-membered ring expands to 6 to relieve angle strain."
            ),
            jeeExceptions = listOf(
                "Cyclopropylmethyl (CPM) carbocation is exceptionally stable due to 'dancing resonance' / bent σ-bond delocalization (even more stable than benzyl carbocation!).",
                "Bridgehead carbocations (Bredt's rule) are extremely unstable because they cannot achieve planar sp² geometry."
            )
        ),
        GocConcept(
            title = "Acidic Strength of Organic Compounds",
            category = "Acidity & Basicity",
            summary = "Acid strength depends directly on the stability of conjugate base (anion formed after loss of H⁺).",
            orderSeriesLatex = "\\text{Acid Strength: } \\text{R-SO}_3\\text{H} > \\text{R-COOH} > \\text{H}_2\\text{CO}_3 > \\text{Phenols} > \\text{H}_2\\text{O} > \\text{ROH} > \\text{HC}\\equiv\\text{CH} > \\text{NH}_3 > \\text{Alkanes}",
            rules = listOf(
                "Carboxylic acid > Phenol > Alcohol: Carboxylate ion has negative charge delocalized over two highly electronegative equivalent oxygen atoms.",
                "Ortho effect in Benzoic Acids: ANY ortho-substituted benzoic acid (-NO₂, -Cl, -CH₃) is STRONGER than benzoic acid, regardless of electronic nature (steric hindrance forces -COOH out of plane, reducing ring resonance and enhancing acidity).",
                "Picric acid (2,4,6-trinitrophenol) is stronger than carboxylic acids due to 3 strong -M/-I nitro groups."
            ),
            jeeExceptions = listOf(
                "Methanol (CH₃OH) is more acidic than water (H₂O), but all other aliphatic alcohols are WEAKER acids than water!",
                "Squaric acid and tropolone are unusually acidic due to aromatic conjugate bases."
            )
        ),
        GocConcept(
            title = "Basic Strength of Amines",
            category = "Acidity & Basicity",
            summary = "Basicity depends on availability of nitrogen lone pair and hydration of conjugate ammonium cation in aqueous solution.",
            orderSeriesLatex = "\\text{Aqueous Basicity: } \\begin{cases} 2^\\circ > 1^\\circ > 3^\\circ > \\text{NH}_3 & (\\text{for } -\\text{CH}_3 \\text{ group}) \\\\ 2^\\circ > 3^\\circ > 1^\\circ > \\text{NH}_3 & (\\text{for } -\\text{C}_2\\text{H}_5 \\text{ group}) \\end{cases}",
            rules = listOf(
                "In gas phase (no solvent): 3° > 2° > 1° > NH₃ (pure +I inductive effect).",
                "In aqueous phase: Net result of three competing factors: Inductive effect (+I), Steric hindrance to protonation, and Hydration energy of conjugate cation.",
                "Aromatic amines (Aniline) are very weak bases because lone pair is delocalized into benzene ring."
            ),
            jeeExceptions = listOf(
                "Ortho effect in Aniline: Ortho-substituted anilines are ALWAYS weaker bases than aniline itself (Steric Inhibition of Protonation - SIP).",
                "Guanidine is one of the strongest organic bases because its protonated conjugate acid has three equivalent resonance structures."
            )
        )
    )

    val ISOMERISM_CONCEPTS: List<IsomerismConcept> = listOf(
        IsomerismConcept(
            title = "Keto-Enol Tautomerism",
            type = "Structural Isomerism",
            definition = "Dynamic equilibrium between a carbonyl compound (keto form) and an unsaturated alcohol (enol form) involving migration of a proton and π-bond.",
            formulaLatex = "\\text{-CH}_2-\\text{C(=O)-} \\rightleftharpoons \\text{-CH=C(OH)-}",
            conditions = listOf(
                "Requires at least one α-hydrogen attached to an sp³ hybridized carbon.",
                "Enol content is normally low (< 1%) for simple aldehydes/ketones because C=O bond is much stronger than C=C bond.",
                "Enol content is very high (> 75%) in β-dicarbonyl compounds (like acetylacetone) due to resonance conjugation and intramolecular hydrogen bonding (pseudo 6-membered ring)."
            ),
            examples = listOf(
                "Acetaldehyde: Keto 99.9%, Enol 0.0001%",
                "Acetylacetone (CH₃COCH₂COCH₃): Enol form ~76% in liquid state (stabilized by intra H-bond)",
                "Phenol is essentially 100% enol because keto form destroys aromaticity."
            )
        ),
        IsomerismConcept(
            title = "Geometrical Isomerism (cis/trans & E/Z)",
            type = "Stereoisomerism",
            definition = "Isomerism arising from restricted rotation about double bonds or rings with different groups on each unsaturated atom.",
            formulaLatex = "\\text{Condition: } ab\\text{C}=\\text{C}cd \\quad (a \\neq b \\text{ and } c \\neq d)",
            conditions = listOf(
                "Restricted rotation around a bond (C=C, C=N, N=N, or cycloalkane ring).",
                "Both groups attached to each doubly bonded carbon must be different (if one carbon has two identical groups, NO geometrical isomerism!).",
                "Cahn-Ingold-Prelog (CIP) Priority Rules for E/Z: Higher atomic number gets higher priority (Priority 1). If both Priority 1 groups are on the SAME side -> (Z) [Zusammen]; if on OPPOSITE sides -> (E) [Entgegen]."
            ),
            examples = listOf(
                "Maleic acid (cis) vs Fumaric acid (trans): Maleic acid easily forms cyclic anhydride on mild heating; Fumaric acid requires very high temp.",
                "cis-But-2-ene has higher boiling point (higher dipole moment) and lower melting point (less symmetrical packing) than trans-But-2-ene."
            )
        ),
        IsomerismConcept(
            title = "Optical Isomerism & Stereoisomer Counting",
            type = "Stereoisomerism",
            definition = "Molecules that lack an alternating axis of symmetry (S_n), plane of symmetry (σ), and center of inversion (i) are chiral and rotate plane-polarized light.",
            formulaLatex = "\\text{For } n \\text{ asymmetric carbons with no symmetry: } N_{\\text{optical}} = 2^n, \\quad N_{\\text{meso}} = 0",
            conditions = listOf(
                "Chiral center (stereocenter): An sp³ carbon bonded to four distinct groups.",
                "Enantiomers: Non-superimposable mirror images with equal and opposite optical rotation ([α]), identical physical properties (BP, MP, density) except rotation of plane-polarized light and reaction with chiral reagents.",
                "Diastereomers: Stereoisomers that are NOT mirror images of each other; have different physical properties (MP, BP, solubility).",
                "Meso compound: Possesses stereocenters but has internal plane/center of symmetry; optically inactive by internal compensation."
            ),
            examples = listOf(
                "Tartaric Acid: HOOC-CH(OH)-CH(OH)-COOH (n=2, symmetrical ends): 2 active enantiomers (d, l) and 1 meso compound (total 3 stereoisomers).",
                "Formula for symmetrical molecules with even n: d+l = 2^(n-1), meso = 2^(n/2 - 1), Total = 2^(n-1) + 2^(n/2 - 1)."
            )
        )
    )

    val NAMED_REACTIONS: List<NamedOrganicReaction> = listOf(
        NamedOrganicReaction(
            name = "Wurtz Reaction",
            reactant = "2 R-X (Alkyl Halides)",
            reagents = "Na / Dry Ether",
            product = "R-R (Higher Symmetrical Alkane) + 2 NaX",
            equationLatex = "2\\,\\text{R-X} + 2\\,\\text{Na} \\xrightarrow{\\text{Dry Ether}} \\text{R-R} + 2\\,\\text{NaX}",
            keyIntermediates = "Free radical (R•) or Organosodium (R⁻Na⁺)",
            importantNotes = "Used to synthesize symmetrical alkanes with even number of carbons. Cannot be used to prepare methane. If two different alkyl halides are used, a mixture of 3 alkanes is obtained which is difficult to separate."
        ),
        NamedOrganicReaction(
            name = "Finkelstein Reaction",
            reactant = "R-Cl or R-Br (Alkyl chloride/bromide)",
            reagents = "NaI in Dry Acetone",
            product = "R-I (Alkyl Iodide) + NaCl/NaBr precipitate",
            equationLatex = "\\text{R-X} + \\text{NaI} \\xrightarrow{\\text{Dry Acetone}} \\text{R-I} + \\text{NaX}\\downarrow \\quad (\\text{X} = \\text{Cl, Br})",
            keyIntermediates = "S_N2 Transition state",
            importantNotes = "Driven forward by Le Chatelier's principle because NaCl and NaBr are insoluble in dry acetone and precipitate out!"
        ),
        NamedOrganicReaction(
            name = "Swarts Reaction",
            reactant = "R-Cl or R-Br",
            reagents = "Metallic Fluorides (AgF, Hg₂F₂, CoF₃, SbF₃)",
            product = "R-F (Alkyl Fluoride)",
            equationLatex = "\\text{R-X} + \\text{AgF} \\rightarrow \\text{R-F} + \\text{AgX}\\downarrow",
            keyIntermediates = "Halogen exchange",
            importantNotes = "Best method for preparation of alkyl fluorides, as direct fluorination of alkanes is violently explosive."
        ),
        NamedOrganicReaction(
            name = "Rosenmund Reduction",
            reactant = "Acyl Chloride (R-COCl)",
            reagents = "H₂ / Pd-BaSO₄, Quinoline / Sulfur (Poisoned catalyst)",
            product = "Aldehyde (R-CHO) + HCl",
            equationLatex = "\\text{R-COCl} + \\text{H}_2 \\xrightarrow{\\text{Pd-BaSO}_4, \\text{quinoline}} \\text{R-CHO} + \\text{HCl}",
            keyIntermediates = "Surface catalytic hydrogenolysis",
            importantNotes = "BaSO₄ and quinoline/sulfur act as catalytic poisons to prevent further reduction of aldehyde into alcohol. Formaldehyde cannot be prepared by this method because formyl chloride is unstable at room temp."
        ),
        NamedOrganicReaction(
            name = "Stephen Reduction",
            reactant = "Nitrile (R-CN)",
            reagents = "1. SnCl₂ + HCl, 2. H₃O⁺",
            product = "Aldehyde (R-CHO) + NH₄Cl",
            equationLatex = "\\text{R-CN} + \\text{SnCl}_2 + 2\\,\\text{HCl} \\rightarrow \\text{R-CH=NH}\\cdot\\text{HCl} \\xrightarrow{\\text{H}_2\\text{O}} \\text{R-CHO} + \\text{NH}_4\\text{Cl}",
            keyIntermediates = "Aldimine hydrochloride intermediate",
            importantNotes = "Mild reduction of cyanides/nitriles specifically to aldehydes."
        ),
        NamedOrganicReaction(
            name = "Clemmensen Reduction",
            reactant = "Aldehyde or Ketone (R-CO-R')",
            reagents = "Zn-Hg (Zinc Amalgam) + Conc. HCl (Δ)",
            product = "Alkane (R-CH₂-R')",
            equationLatex = "\\text{R-CO-R}' + 4[\\text{H}] \\xrightarrow{\\text{Zn-Hg, Conc. HCl}} \\text{R-CH}_2\\text{-R}' + \\text{H}_2\\text{O}",
            keyIntermediates = "Zinc carbenoid on metal surface",
            importantNotes = "USED FOR ACID-STABLE COMPOUNDS. Do NOT use if acid-sensitive groups are present (-OH, -C=C-, -C≡C- will react with conc. HCl)."
        ),
        NamedOrganicReaction(
            name = "Wolff-Kishner Reduction",
            reactant = "Aldehyde or Ketone",
            reagents = "NH₂-NH₂ (Hydrazine) followed by KOH / Ethylene Glycol (Δ, 450 K)",
            product = "Alkane + N₂ gas",
            equationLatex = "\\text{R-CO-R}' \\xrightarrow{\\text{NH}_2\\text{NH}_2} \\text{R-C(=NNH}_2)\\text{R}' \\xrightarrow{\\text{KOH, Glycol, } \\Delta} \\text{R-CH}_2\\text{-R}' + \\text{N}_2\\uparrow",
            keyIntermediates = "Hydrazone intermediate followed by carbanion",
            importantNotes = "USED FOR BASE-STABLE COMPOUNDS. Ideal when acid-sensitive groups are present in the molecule. Driven to completion by escape of stable N₂ gas."
        ),
        NamedOrganicReaction(
            name = "Hell-Volhard-Zelinsky (HVZ) Reaction",
            reactant = "Carboxylic acid with α-hydrogen (R-CH₂-COOH)",
            reagents = "1. X₂ (Cl₂ or Br₂) / Red Phosphorus, 2. H₂O",
            product = "α-Halocarboxylic acid (R-CH(X)-COOH)",
            equationLatex = "\\text{R-CH}_2\\text{COOH} \\xrightarrow{1.\\,\\text{X}_2/\\text{Red P},\\, 2.\\,\\text{H}_2\\text{O}} \\text{R-CH(X)COOH}",
            keyIntermediates = "Acyl halide (R-CH₂-COX) and its enol form",
            importantNotes = "Specifically halogenates the α-carbon of carboxylic acids. Requires at least one α-hydrogen (formic acid and benzoic acid do NOT give HVZ reaction)."
        ),
        NamedOrganicReaction(
            name = "Gabriel Phthalimide Synthesis",
            reactant = "Phthalimide + 1° Alkyl Halide (R-X)",
            reagents = "1. KOH, 2. R-X, 3. NaOH / H₂O (Hydrazine)",
            product = "Pure 1° Aliphatic Amine (R-NH₂)",
            equationLatex = "\\text{Phthalimide} \\xrightarrow{\\text{KOH}} \\text{Potassium phthalimide} \\xrightarrow{\\text{R-X (S}_N2)} N\\text{-alkylphthalimide} \\xrightarrow{\\text{NaOH}} \\text{R-NH}_2 + \\text{Phthalate}",
            keyIntermediates = "Phthalimide anion",
            importantNotes = "EXCLUSIVELY prepares pure PRIMARY aliphatic amines without 2° or 3° amine contamination. Aryl halides and vinyl halides CANNOT be used because they do not undergo S_N2 substitution."
        ),
        NamedOrganicReaction(
            name = "Carbylamine Test (Isocyanide Test)",
            reactant = "1° Amine (Aliphatic or Aromatic)",
            reagents = "CHCl₃ + 3 KOH (alcoholic, Δ)",
            product = "Isocyanide / Carbylamine (R-NC, extremely foul odor) + 3 KCl + 3 H₂O",
            equationLatex = "\\text{R-NH}_2 + \\text{CHCl}_3 + 3\\,\\text{KOH} \\xrightarrow{\\Delta} \\text{R-NC} (\\text{foul smelling}) + 3\\,\\text{KCl} + 3\\,\\text{H}_2\\text{O}",
            keyIntermediates = "Dichlorocarbene (:CCl₂)",
            importantNotes = "DISTINCTION TEST: Positive test given ONLY by primary amines (both aliphatic and aromatic like aniline). 2° and 3° amines give NEGATIVE test."
        )
    )

    val FUNCTIONAL_TESTS: List<FunctionalGroupTest> = listOf(
        FunctionalGroupTest(
            functionalGroup = "Alcohols (1°, 2°, 3°)",
            testName = "Lucas Test",
            reagentUsed = "Lucas Reagent: Conc. HCl + Anhydrous ZnCl₂",
            positiveObservation = "Turbidity (insoluble alkyl chloride formation):\n• 3° Alcohols: Immediate turbidity at room temp\n• 2° Alcohols: Turbidity appears within 5 minutes\n• 1° Alcohols: No turbidity at room temp (only upon heating)",
            equationLatex = "\\text{R-OH} + \\text{HCl} \\xrightarrow{\\text{anh. ZnCl}_2} \\text{R-Cl}\\downarrow (\\text{turbidity}) + \\text{H}_2\\text{O}",
            notes = "Based on carbocation stability (S_N1 mechanism): 3° > 2° > 1°."
        ),
        FunctionalGroupTest(
            functionalGroup = "Aldehydes",
            testName = "Tollens' Test (Silver Mirror Test)",
            reagentUsed = "Ammoniacal Silver Nitrate [Ag(NH₃)₂]⁺ OH⁻",
            positiveObservation = "Bright shining Silver Mirror formed on inner walls of test tube",
            equationLatex = "\\text{R-CHO} + 2[\\text{Ag(NH}_3)_2]^+ + 3\\,\\text{OH}^- \\rightarrow \\text{R-COO}^- + 2\\,\\text{Ag}\\downarrow (\\text{silver mirror}) + 4\\,\\text{NH}_3 + 2\\,\\text{H}_2\\text{O}",
            notes = "Both aliphatic and aromatic aldehydes give positive Tollens' test. Ketones do NOT react (except α-hydroxy ketones)."
        ),
        FunctionalGroupTest(
            functionalGroup = "Aliphatic Aldehydes",
            testName = "Fehling's Test",
            reagentUsed = "Fehling A (aq. CuSO₄) + Fehling B (alkaline Sodium Potassium Tartrate / Rochelle salt)",
            positiveObservation = "Red-brown precipitate of Cuprous Oxide (Cu₂O)",
            equationLatex = "\\text{R-CHO} + 2\\,\\text{Cu}^{2+} + 5\\,\\text{OH}^- \\rightarrow \\text{R-COO}^- + \\text{Cu}_2\\text{O}\\downarrow (\\text{red}) + 3\\,\\text{H}_2\\text{O}",
            notes = "CRUCIAL JEE TRAP: Benzaldehyde and other AROMATIC aldehydes do NOT reduce Fehling's solution!"
        ),
        FunctionalGroupTest(
            functionalGroup = "Methyl Ketones / CH₃-CH(OH)-",
            testName = "Iodoform Test",
            reagentUsed = "I₂ + NaOH (or NaOI)",
            positiveObservation = "Bright yellow crystalline precipitate of Iodoform (CHI₃) with antiseptic odor (MP 119°C)",
            equationLatex = "\\text{R-CO-CH}_3 + 3\\,\\text{I}_2 + 4\\,\\text{OH}^- \\rightarrow \\text{R-COO}^- + \\text{CHI}_3\\downarrow (\\text{yellow}) + 3\\,\\text{I}^- + 3\\,\\text{H}_2\\text{O}",
            notes = "Positive with: ethanol, acetaldehyde, all methyl ketones (2-pentanone), secondary alcohols with CH₃-CH(OH)- group."
        ),
        FunctionalGroupTest(
            functionalGroup = "Amines (1°, 2°, 3°)",
            testName = "Hinsberg Test",
            reagentUsed = "Benzenesulphonyl chloride (C₆H₅SO₂Cl)",
            positiveObservation = "• 1° Amine: Forms N-alkylbenzenesulphonamide, which is SOLUBLE in alkali (has acidic H on nitrogen).\n• 2° Amine: Forms N,N-dialkylbenzenesulphonamide, which is INSOLUBLE in alkali (no H on nitrogen).\n• 3° Amine: Does NOT react with Hinsberg reagent.",
            equationLatex = "\\text{C}_6\\text{H}_5\\text{SO}_2\\text{Cl} + \\text{R-NH}_2 \\rightarrow \\text{C}_6\\text{H}_5\\text{SO}_2\\text{NHR} \\xrightarrow{\\text{KOH}} \\text{Soluble salt}",
            notes = "Standard analytical method for separation and identification of primary, secondary, and tertiary amine mixtures."
        ),
        FunctionalGroupTest(
            functionalGroup = "Phenols",
            testName = "Neutral Ferric Chloride Test",
            reagentUsed = "Freshly prepared Neutral FeCl₃ solution",
            positiveObservation = "Characteristic Violet, Blue, or Green coloration due to formation of iron coordination complex",
            equationLatex = "6\\,\\text{C}_6\\text{H}_5\\text{OH} + \\text{Fe}^{3+} \\rightarrow [\\text{Fe(OC}_6\\text{H}_5)_6]^{3-} (\\text{violet}) + 6\\,\\text{H}^+",
            notes = "Alcohols do NOT show this color reaction; specific for enols and phenolic -OH groups."
        ),
        FunctionalGroupTest(
            functionalGroup = "Carboxylic Acids",
            testName = "Sodium Bicarbonate Test",
            reagentUsed = "Aqueous Sodium Bicarbonate (NaHCO₃)",
            positiveObservation = "Brisk effervescence of colorless, odorless Carbon Dioxide (CO₂) gas which turns lime water milky",
            equationLatex = "\\text{R-COOH} + \\text{NaHCO}_3 \\rightarrow \\text{R-COONa} + \\text{H}_2\\text{O} + \\text{CO}_2\\uparrow",
            notes = "Phenols do NOT liberate CO₂ with NaHCO₃ (except picric acid and 2,4-dinitrophenol, which are more acidic than H₂CO₃)."
        ),
        FunctionalGroupTest(
            functionalGroup = "Unsaturation (Alkenes & Alkynes)",
            testName = "Baeyer's Test",
            reagentUsed = "Cold dilute alkaline 1% KMnO₄ solution (Pink)",
            positiveObservation = "Decolorization of pink KMnO₄ with brown precipitate of Manganese Dioxide (MnO₂)",
            equationLatex = "\\text{CH}_2=\\text{CH}_2 + \\text{H}_2\\text{O} + [\\text{O}] \\xrightarrow{\\text{alk. KMnO}_4} \\text{CH}_2(\\text{OH})-\\text{CH}_2(\\text{OH}) \\text{ (syn-diol)}",
            notes = "Syn-hydroxylation of alkene to vicinal glycol. Also positive with aldehydes."
        )
    )

    val REAGENT_SELECTIVITIES: List<ReagentSelectivity> = listOf(
        ReagentSelectivity(
            reagent = "LiAlH₄ (Lithium Aluminium Hydride)",
            role = "Strong, Non-selective Reducing Agent",
            functionalGroupAction = "Reduces aldehydes, ketones, carboxylic acids, esters, acid chlorides, amides, and nitriles to alcohols/amines.",
            exceptionsAndSelectivity = "Does NOT reduce isolated C=C or C≡C bonds (except when conjugated with phenyl group like cinnamic acid)."
        ),
        ReagentSelectivity(
            reagent = "NaBH₄ (Sodium Borohydride)",
            role = "Mild, Selective Reducing Agent",
            functionalGroupAction = "Reduces ONLY aldehydes, ketones, and acid chlorides to alcohols. Safe in protic solvents (EtOH, H₂O).",
            exceptionsAndSelectivity = "Does NOT reduce carboxylic acids, esters, amides, nitriles, or alkenes."
        ),
        ReagentSelectivity(
            reagent = "PCC (Pyridinium Chlorochromate)",
            role = "Mild Selective Oxidizing Agent in CH₂Cl₂",
            functionalGroupAction = "Oxidizes 1° alcohols strictly to aldehydes (prevents further oxidation to carboxylic acids); oxidizes 2° alcohols to ketones.",
            exceptionsAndSelectivity = "Anhydrous conditions prevent hydrate formation and subsequent oxidation to acid."
        ),
        ReagentSelectivity(
            reagent = "DIBAL-H (Diisobutylaluminium Hydride) at -78°C",
            role = "Sterically Hindered Selective Reducing Agent",
            functionalGroupAction = "Reduces Esters and Nitriles specifically to ALDEHYDES at low temperature (-78°C).",
            exceptionsAndSelectivity = "At room temperature, acts as strong reducing agent reducing to alcohols."
        ),
        ReagentSelectivity(
            reagent = "Ozonolysis: 1. O₃, 2. Zn / H₂O (Reductive)",
            role = "Oxidative Cleavage of Alkenes",
            functionalGroupAction = "Cleaves C=C bond to aldehydes and ketones without over-oxidation.",
            exceptionsAndSelectivity = "If Oxidative workup (H₂O₂ or hot alkaline KMnO₄) is used, aldehydes are oxidized to carboxylic acids!"
        )
    )

    // Quantitative Organic Chemistry Calculations
    fun calculateCombustionAnalysis(
        massOrganicCompound: Double,
        massCO2: Double,
        massH2O: Double
    ): OrganicCalcResult {
        if (massOrganicCompound <= 0.0) {
            return OrganicCalcResult("Combustion Analysis", "", emptyList(), "Mass of compound must be positive")
        }
        val percentC = (12.0 / 44.0) * (massCO2 / massOrganicCompound) * 100.0
        val percentH = (2.016 / 18.016) * (massH2O / massOrganicCompound) * 100.0
        val percentO = max(0.0, 100.0 - (percentC + percentH))

        val steps = listOf(
            "Step 1: Formula for Carbon %: \\%\\text{C} = \\frac{12}{44} \\times \\frac{m_{\\text{CO}_2}}{m_{\\text{sample}}} \\times 100",
            "Substitution: \\%\\text{C} = \\frac{12}{44} \\times \\frac{$massCO2}{$massOrganicCompound} \\times 100 = ${round(percentC * 100) / 100}\\%",
            "Step 2: Formula for Hydrogen %: \\%\\text{H} = \\frac{2.016}{18.016} \\times \\frac{m_{\\text{H}_2\\text{O}}}{m_{\\text{sample}}} \\times 100",
            "Substitution: \\%\\text{H} = \\frac{2.016}{18.016} \\times \\frac{$massH2O}{$massOrganicCompound} \\times 100 = ${round(percentH * 100) / 100}\\%",
            "Step 3: Oxygen by difference: \\%\\text{O} = 100 - (\\%\\text{C} + \\%\\text{H}) = ${round(percentO * 100) / 100}\\%"
        )
        val answer = "Carbon: ${round(percentC * 100) / 100}%, Hydrogen: ${round(percentH * 100) / 100}%, Oxygen: ${round(percentO * 100) / 100}%"
        return OrganicCalcResult(
            title = "Liebig's Combustion Analysis (%C, %H, %O)",
            formulaLatex = "\\%\\text{C} = \\frac{12}{44}\\frac{m_{\\text{CO}_2}}{m}\\times 100, \\quad \\%\\text{H} = \\frac{2}{18}\\frac{m_{\\text{H}_2\\text{O}}}{m}\\times 100",
            steps = steps,
            finalAnswer = answer
        )
    }

    fun calculateKjeldahl(
        massSample: Double,
        volAcidUsedMl: Double,
        normalityAcid: Double
    ): OrganicCalcResult {
        if (massSample <= 0.0) {
            return OrganicCalcResult("Kjeldahl's Method", "", emptyList(), "Invalid sample mass")
        }
        val percentN = (1.4 * normalityAcid * volAcidUsedMl) / massSample
        val steps = listOf(
            "Step 1: Nitrogen is converted to ammonium sulphate: (NH₄)₂SO₄, then distilled with NaOH into standard acid.",
            "Step 2: Kjeldahl formula: \\%\\text{N} = \\frac{1.4 \\times N \\times V_{\\text{acid (mL)}}}{m_{\\text{sample (g)}}}",
            "Substitution: \\%\\text{N} = \\frac{1.4 \\times $normalityAcid \\times $volAcidUsedMl}{$massSample} = ${round(percentN * 100) / 100}\\%"
        )
        return OrganicCalcResult(
            title = "Kjeldahl's Method for Nitrogen Percentage",
            formulaLatex = "\\%\\text{N} = \\frac{1.4 \\times N \\times V}{m}",
            steps = steps,
            finalAnswer = "Nitrogen: ${round(percentN * 100) / 100}%"
        )
    }

    fun calculateCariusHalogen(
        massSample: Double,
        massAgX: Double,
        halogenType: String // "Cl", "Br", "I"
    ): OrganicCalcResult {
        if (massSample <= 0.0) {
            return OrganicCalcResult("Carius Method", "", emptyList(), "Invalid sample mass")
        }
        val (atomicMassX, molarMassAgX) = when (halogenType.uppercase()) {
            "BR" -> Pair(80.0, 188.0)
            "I" -> Pair(127.0, 235.0)
            else -> Pair(35.5, 143.5)
        }
        val percentX = (atomicMassX / molarMassAgX) * (massAgX / massSample) * 100.0
        val steps = listOf(
            "Step 1: Organic compound heated with fuming HNO₃ and AgNO₃ in sealed Carius tube forming AgX precipitate.",
            "Step 2: Formula: \\%\\text{X} = \\frac{\\text{At. Mass of X}}{\\text{Molar Mass of AgX}} \\times \\frac{m_{\\text{AgX}}}{m_{\\text{sample}}} \\times 100",
            "Substitution: \\%\\text{$halogenType} = \\frac{$atomicMassX}{$molarMassAgX} \\times \\frac{$massAgX}{$massSample} \\times 100 = ${round(percentX * 100) / 100}\\%"
        )
        return OrganicCalcResult(
            title = "Carius Method for Halogen Percentage ($halogenType)",
            formulaLatex = "\\%\\text{X} = \\frac{M_X}{M_{\\text{AgX}}} \\times \\frac{m_{\\text{AgX}}}{m} \\times 100",
            steps = steps,
            finalAnswer = "Halogen ($halogenType): ${round(percentX * 100) / 100}%"
        )
    }

    fun calculateDegreeOfUnsaturation(c: Int, h: Int, n: Int = 0, x: Int = 0): Int {
        // DBE = C + 1 - (H + X - N)/2
        return c + 1 - (h + x - n) / 2
    }
}
