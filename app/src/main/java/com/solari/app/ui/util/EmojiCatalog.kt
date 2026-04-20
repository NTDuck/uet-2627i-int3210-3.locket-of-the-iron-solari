package com.solari.app.ui.util

data class EmojiCatalogItem(
    val emoji: String,
    val name: String
)

data class EmojiCatalogCategory(
    val name: String,
    val emojis: List<EmojiCatalogItem>
)

object EmojiCatalog {
    val categories: List<EmojiCatalogCategory> by lazy(LazyThreadSafetyMode.NONE) {
        parseEmojiData(EmojiDataChunks)
    }

    private fun parseEmojiData(chunks: List<String>): List<EmojiCatalogCategory> {
        val grouped = linkedMapOf<String, MutableList<EmojiCatalogItem>>()
        chunks.forEach { chunk ->
            chunk.lineSequence().forEach { line ->
                val fields = line.split('\t', limit = 3)
                if (fields.size == 3) {
                    grouped.getOrPut(fields[0]) { mutableListOf() } += EmojiCatalogItem(
                        emoji = fields[1],
                        name = fields[2]
                    )
                }
            }
        }

        return grouped.map { (name, emojis) ->
            EmojiCatalogCategory(name = name, emojis = emojis)
        }
    }

    private val EmojiDataChunks: List<String> = listOf(
        """
Smileys & Emotion	😀	grinning face
Smileys & Emotion	😃	grinning face with big eyes
Smileys & Emotion	😄	grinning face with smiling eyes
Smileys & Emotion	😁	beaming face with smiling eyes
Smileys & Emotion	😆	grinning squinting face
Smileys & Emotion	😅	grinning face with sweat
Smileys & Emotion	🤣	rolling on the floor laughing
Smileys & Emotion	😂	face with tears of joy
Smileys & Emotion	🙂	slightly smiling face
Smileys & Emotion	🙃	upside-down face
Smileys & Emotion	🫠	melting face
Smileys & Emotion	😉	winking face
Smileys & Emotion	😊	smiling face with smiling eyes
Smileys & Emotion	😇	smiling face with halo
Smileys & Emotion	🥰	smiling face with hearts
Smileys & Emotion	😍	smiling face with heart-eyes
Smileys & Emotion	🤩	star-struck
Smileys & Emotion	😘	face blowing a kiss
Smileys & Emotion	😗	kissing face
Smileys & Emotion	☺️	smiling face
Smileys & Emotion	😚	kissing face with closed eyes
Smileys & Emotion	😙	kissing face with smiling eyes
Smileys & Emotion	🥲	smiling face with tear
Smileys & Emotion	😋	face savoring food
Smileys & Emotion	😛	face with tongue
Smileys & Emotion	😜	winking face with tongue
Smileys & Emotion	🤪	zany face
Smileys & Emotion	😝	squinting face with tongue
Smileys & Emotion	🤑	money-mouth face
Smileys & Emotion	🤗	smiling face with open hands
Smileys & Emotion	🤭	face with hand over mouth
Smileys & Emotion	🫢	face with open eyes and hand over mouth
Smileys & Emotion	🫣	face with peeking eye
Smileys & Emotion	🤫	shushing face
Smileys & Emotion	🤔	thinking face
Smileys & Emotion	🫡	saluting face
Smileys & Emotion	🤐	zipper-mouth face
Smileys & Emotion	🤨	face with raised eyebrow
Smileys & Emotion	😐	neutral face
Smileys & Emotion	😑	expressionless face
Smileys & Emotion	😶	face without mouth
Smileys & Emotion	🫥	dotted line face
Smileys & Emotion	😶‍🌫️	face in clouds
Smileys & Emotion	😏	smirking face
Smileys & Emotion	😒	unamused face
Smileys & Emotion	🙄	face with rolling eyes
Smileys & Emotion	😬	grimacing face
Smileys & Emotion	😮‍💨	face exhaling
Smileys & Emotion	🤥	lying face
Smileys & Emotion	🫨	shaking face
Smileys & Emotion	🙂‍↔️	head shaking horizontally
Smileys & Emotion	🙂‍↕️	head shaking vertically
Smileys & Emotion	😌	relieved face
Smileys & Emotion	😔	pensive face
Smileys & Emotion	😪	sleepy face
Smileys & Emotion	🤤	drooling face
Smileys & Emotion	😴	sleeping face
Smileys & Emotion	🫩	face with bags under eyes
Smileys & Emotion	😷	face with medical mask
Smileys & Emotion	🤒	face with thermometer
Smileys & Emotion	🤕	face with head-bandage
Smileys & Emotion	🤢	nauseated face
Smileys & Emotion	🤮	face vomiting
Smileys & Emotion	🤧	sneezing face
Smileys & Emotion	🥵	hot face
Smileys & Emotion	🥶	cold face
Smileys & Emotion	🥴	woozy face
Smileys & Emotion	😵	face with crossed-out eyes
Smileys & Emotion	😵‍💫	face with spiral eyes
Smileys & Emotion	🤯	exploding head
Smileys & Emotion	🤠	cowboy hat face
Smileys & Emotion	🥳	partying face
Smileys & Emotion	🥸	disguised face
Smileys & Emotion	😎	smiling face with sunglasses
Smileys & Emotion	🤓	nerd face
Smileys & Emotion	🧐	face with monocle
Smileys & Emotion	😕	confused face
Smileys & Emotion	🫤	face with diagonal mouth
Smileys & Emotion	😟	worried face
Smileys & Emotion	🙁	slightly frowning face
Smileys & Emotion	☹️	frowning face
Smileys & Emotion	😮	face with open mouth
Smileys & Emotion	😯	hushed face
Smileys & Emotion	😲	astonished face
Smileys & Emotion	😳	flushed face
Smileys & Emotion	🫪	distorted face
Smileys & Emotion	🥺	pleading face
Smileys & Emotion	🥹	face holding back tears
Smileys & Emotion	😦	frowning face with open mouth
Smileys & Emotion	😧	anguished face
Smileys & Emotion	😨	fearful face
Smileys & Emotion	😰	anxious face with sweat
Smileys & Emotion	😥	sad but relieved face
Smileys & Emotion	😢	crying face
Smileys & Emotion	😭	loudly crying face
Smileys & Emotion	😱	face screaming in fear
Smileys & Emotion	😖	confounded face
Smileys & Emotion	😣	persevering face
Smileys & Emotion	😞	disappointed face
Smileys & Emotion	😓	downcast face with sweat
Smileys & Emotion	😩	weary face
Smileys & Emotion	😫	tired face
Smileys & Emotion	🥱	yawning face
Smileys & Emotion	😤	face with steam from nose
Smileys & Emotion	😡	enraged face
Smileys & Emotion	😠	angry face
Smileys & Emotion	🤬	face with symbols on mouth
Smileys & Emotion	😈	smiling face with horns
Smileys & Emotion	👿	angry face with horns
Smileys & Emotion	💀	skull
Smileys & Emotion	☠️	skull and crossbones
Smileys & Emotion	💩	pile of poo
Smileys & Emotion	🤡	clown face
Smileys & Emotion	👹	ogre
Smileys & Emotion	👺	goblin
Smileys & Emotion	👻	ghost
Smileys & Emotion	👽	alien
Smileys & Emotion	👾	alien monster
Smileys & Emotion	🤖	robot
Smileys & Emotion	😺	grinning cat
Smileys & Emotion	😸	grinning cat with smiling eyes
Smileys & Emotion	😹	cat with tears of joy
Smileys & Emotion	😻	smiling cat with heart-eyes
Smileys & Emotion	😼	cat with wry smile
Smileys & Emotion	😽	kissing cat
Smileys & Emotion	🙀	weary cat
Smileys & Emotion	😿	crying cat
Smileys & Emotion	😾	pouting cat
Smileys & Emotion	🙈	see-no-evil monkey
Smileys & Emotion	🙉	hear-no-evil monkey
Smileys & Emotion	🙊	speak-no-evil monkey
Smileys & Emotion	💌	love letter
Smileys & Emotion	💘	heart with arrow
Smileys & Emotion	💝	heart with ribbon
Smileys & Emotion	💖	sparkling heart
Smileys & Emotion	💗	growing heart
Smileys & Emotion	💓	beating heart
Smileys & Emotion	💞	revolving hearts
Smileys & Emotion	💕	two hearts
Smileys & Emotion	💟	heart decoration
Smileys & Emotion	❣️	heart exclamation
Smileys & Emotion	💔	broken heart
Smileys & Emotion	❤️‍🔥	heart on fire
Smileys & Emotion	❤️‍🩹	mending heart
Smileys & Emotion	❤️	red heart
Smileys & Emotion	🩷	pink heart
Smileys & Emotion	🧡	orange heart
Smileys & Emotion	💛	yellow heart
Smileys & Emotion	💚	green heart
Smileys & Emotion	💙	blue heart
Smileys & Emotion	🩵	light blue heart
Smileys & Emotion	💜	purple heart
Smileys & Emotion	🤎	brown heart
Smileys & Emotion	🖤	black heart
Smileys & Emotion	🩶	grey heart
Smileys & Emotion	🤍	white heart
Smileys & Emotion	💋	kiss mark
Smileys & Emotion	💯	hundred points
Smileys & Emotion	💢	anger symbol
Smileys & Emotion	🫯	fight cloud
Smileys & Emotion	💥	collision
Smileys & Emotion	💫	dizzy
Smileys & Emotion	💦	sweat droplets
Smileys & Emotion	💨	dashing away
Smileys & Emotion	🕳️	hole
Smileys & Emotion	💬	speech balloon
Smileys & Emotion	👁️‍🗨️	eye in speech bubble
Smileys & Emotion	🗨️	left speech bubble
Smileys & Emotion	🗯️	right anger bubble
Smileys & Emotion	💭	thought balloon
Smileys & Emotion	💤	ZZZ
People & Body	👋	waving hand
People & Body	👋🏻	waving hand: light skin tone
People & Body	👋🏼	waving hand: medium-light skin tone
People & Body	👋🏽	waving hand: medium skin tone
People & Body	👋🏾	waving hand: medium-dark skin tone
People & Body	👋🏿	waving hand: dark skin tone
People & Body	🤚	raised back of hand
People & Body	🤚🏻	raised back of hand: light skin tone
People & Body	🤚🏼	raised back of hand: medium-light skin tone
People & Body	🤚🏽	raised back of hand: medium skin tone
People & Body	🤚🏾	raised back of hand: medium-dark skin tone
People & Body	🤚🏿	raised back of hand: dark skin tone
People & Body	🖐️	hand with fingers splayed
People & Body	🖐🏻	hand with fingers splayed: light skin tone
People & Body	🖐🏼	hand with fingers splayed: medium-light skin tone
People & Body	🖐🏽	hand with fingers splayed: medium skin tone
People & Body	🖐🏾	hand with fingers splayed: medium-dark skin tone
People & Body	🖐🏿	hand with fingers splayed: dark skin tone
People & Body	✋	raised hand
People & Body	✋🏻	raised hand: light skin tone
People & Body	✋🏼	raised hand: medium-light skin tone
People & Body	✋🏽	raised hand: medium skin tone
People & Body	✋🏾	raised hand: medium-dark skin tone
People & Body	✋🏿	raised hand: dark skin tone
People & Body	🖖	vulcan salute
People & Body	🖖🏻	vulcan salute: light skin tone
People & Body	🖖🏼	vulcan salute: medium-light skin tone
People & Body	🖖🏽	vulcan salute: medium skin tone
People & Body	🖖🏾	vulcan salute: medium-dark skin tone
People & Body	🖖🏿	vulcan salute: dark skin tone
People & Body	🫱	rightwards hand
People & Body	🫱🏻	rightwards hand: light skin tone
People & Body	🫱🏼	rightwards hand: medium-light skin tone
People & Body	🫱🏽	rightwards hand: medium skin tone
People & Body	🫱🏾	rightwards hand: medium-dark skin tone
People & Body	🫱🏿	rightwards hand: dark skin tone
People & Body	🫲	leftwards hand
People & Body	🫲🏻	leftwards hand: light skin tone
People & Body	🫲🏼	leftwards hand: medium-light skin tone
People & Body	🫲🏽	leftwards hand: medium skin tone
People & Body	🫲🏾	leftwards hand: medium-dark skin tone
People & Body	🫲🏿	leftwards hand: dark skin tone
People & Body	🫳	palm down hand
People & Body	🫳🏻	palm down hand: light skin tone
People & Body	🫳🏼	palm down hand: medium-light skin tone
People & Body	🫳🏽	palm down hand: medium skin tone
People & Body	🫳🏾	palm down hand: medium-dark skin tone
People & Body	🫳🏿	palm down hand: dark skin tone
People & Body	🫴	palm up hand
People & Body	🫴🏻	palm up hand: light skin tone
People & Body	🫴🏼	palm up hand: medium-light skin tone
People & Body	🫴🏽	palm up hand: medium skin tone
People & Body	🫴🏾	palm up hand: medium-dark skin tone
People & Body	🫴🏿	palm up hand: dark skin tone
People & Body	🫷	leftwards pushing hand
People & Body	🫷🏻	leftwards pushing hand: light skin tone
People & Body	🫷🏼	leftwards pushing hand: medium-light skin tone
People & Body	🫷🏽	leftwards pushing hand: medium skin tone
People & Body	🫷🏾	leftwards pushing hand: medium-dark skin tone
People & Body	🫷🏿	leftwards pushing hand: dark skin tone
People & Body	🫸	rightwards pushing hand
People & Body	🫸🏻	rightwards pushing hand: light skin tone
People & Body	🫸🏼	rightwards pushing hand: medium-light skin tone
People & Body	🫸🏽	rightwards pushing hand: medium skin tone
People & Body	🫸🏾	rightwards pushing hand: medium-dark skin tone
People & Body	🫸🏿	rightwards pushing hand: dark skin tone
People & Body	👌	OK hand
People & Body	👌🏻	OK hand: light skin tone
People & Body	👌🏼	OK hand: medium-light skin tone
People & Body	👌🏽	OK hand: medium skin tone
People & Body	👌🏾	OK hand: medium-dark skin tone
People & Body	👌🏿	OK hand: dark skin tone
People & Body	🤌	pinched fingers
People & Body	🤌🏻	pinched fingers: light skin tone
People & Body	🤌🏼	pinched fingers: medium-light skin tone
People & Body	🤌🏽	pinched fingers: medium skin tone
People & Body	🤌🏾	pinched fingers: medium-dark skin tone
People & Body	🤌🏿	pinched fingers: dark skin tone
People & Body	🤏	pinching hand
People & Body	🤏🏻	pinching hand: light skin tone
People & Body	🤏🏼	pinching hand: medium-light skin tone
People & Body	🤏🏽	pinching hand: medium skin tone
People & Body	🤏🏾	pinching hand: medium-dark skin tone
People & Body	🤏🏿	pinching hand: dark skin tone
People & Body	✌️	victory hand
People & Body	✌🏻	victory hand: light skin tone
People & Body	✌🏼	victory hand: medium-light skin tone
People & Body	✌🏽	victory hand: medium skin tone
People & Body	✌🏾	victory hand: medium-dark skin tone
People & Body	✌🏿	victory hand: dark skin tone
People & Body	🤞	crossed fingers
People & Body	🤞🏻	crossed fingers: light skin tone
People & Body	🤞🏼	crossed fingers: medium-light skin tone
People & Body	🤞🏽	crossed fingers: medium skin tone
People & Body	🤞🏾	crossed fingers: medium-dark skin tone
People & Body	🤞🏿	crossed fingers: dark skin tone
People & Body	🫰	hand with index finger and thumb crossed
People & Body	🫰🏻	hand with index finger and thumb crossed: light skin tone
People & Body	🫰🏼	hand with index finger and thumb crossed: medium-light skin tone
People & Body	🫰🏽	hand with index finger and thumb crossed: medium skin tone
People & Body	🫰🏾	hand with index finger and thumb crossed: medium-dark skin tone
People & Body	🫰🏿	hand with index finger and thumb crossed: dark skin tone
People & Body	🤟	love-you gesture
People & Body	🤟🏻	love-you gesture: light skin tone
People & Body	🤟🏼	love-you gesture: medium-light skin tone
People & Body	🤟🏽	love-you gesture: medium skin tone
People & Body	🤟🏾	love-you gesture: medium-dark skin tone
People & Body	🤟🏿	love-you gesture: dark skin tone
People & Body	🤘	sign of the horns
People & Body	🤘🏻	sign of the horns: light skin tone
People & Body	🤘🏼	sign of the horns: medium-light skin tone
People & Body	🤘🏽	sign of the horns: medium skin tone
People & Body	🤘🏾	sign of the horns: medium-dark skin tone
People & Body	🤘🏿	sign of the horns: dark skin tone
People & Body	🤙	call me hand
People & Body	🤙🏻	call me hand: light skin tone
People & Body	🤙🏼	call me hand: medium-light skin tone
People & Body	🤙🏽	call me hand: medium skin tone
People & Body	🤙🏾	call me hand: medium-dark skin tone
People & Body	🤙🏿	call me hand: dark skin tone
People & Body	👈	backhand index pointing left
People & Body	👈🏻	backhand index pointing left: light skin tone
People & Body	👈🏼	backhand index pointing left: medium-light skin tone
People & Body	👈🏽	backhand index pointing left: medium skin tone
People & Body	👈🏾	backhand index pointing left: medium-dark skin tone
People & Body	👈🏿	backhand index pointing left: dark skin tone
People & Body	👉	backhand index pointing right
People & Body	👉🏻	backhand index pointing right: light skin tone
People & Body	👉🏼	backhand index pointing right: medium-light skin tone
People & Body	👉🏽	backhand index pointing right: medium skin tone
People & Body	👉🏾	backhand index pointing right: medium-dark skin tone
People & Body	👉🏿	backhand index pointing right: dark skin tone
People & Body	👆	backhand index pointing up
People & Body	👆🏻	backhand index pointing up: light skin tone
People & Body	👆🏼	backhand index pointing up: medium-light skin tone
People & Body	👆🏽	backhand index pointing up: medium skin tone
People & Body	👆🏾	backhand index pointing up: medium-dark skin tone
People & Body	👆🏿	backhand index pointing up: dark skin tone
People & Body	🖕	middle finger
People & Body	🖕🏻	middle finger: light skin tone
People & Body	🖕🏼	middle finger: medium-light skin tone
People & Body	🖕🏽	middle finger: medium skin tone
People & Body	🖕🏾	middle finger: medium-dark skin tone
People & Body	🖕🏿	middle finger: dark skin tone
People & Body	👇	backhand index pointing down
People & Body	👇🏻	backhand index pointing down: light skin tone
People & Body	👇🏼	backhand index pointing down: medium-light skin tone
People & Body	👇🏽	backhand index pointing down: medium skin tone
People & Body	👇🏾	backhand index pointing down: medium-dark skin tone
People & Body	👇🏿	backhand index pointing down: dark skin tone
People & Body	☝️	index pointing up
People & Body	☝🏻	index pointing up: light skin tone
People & Body	☝🏼	index pointing up: medium-light skin tone
People & Body	☝🏽	index pointing up: medium skin tone
People & Body	☝🏾	index pointing up: medium-dark skin tone
People & Body	☝🏿	index pointing up: dark skin tone
People & Body	🫵	index pointing at the viewer
People & Body	🫵🏻	index pointing at the viewer: light skin tone
People & Body	🫵🏼	index pointing at the viewer: medium-light skin tone
People & Body	🫵🏽	index pointing at the viewer: medium skin tone
People & Body	🫵🏾	index pointing at the viewer: medium-dark skin tone
People & Body	🫵🏿	index pointing at the viewer: dark skin tone
People & Body	👍	thumbs up
People & Body	👍🏻	thumbs up: light skin tone
People & Body	👍🏼	thumbs up: medium-light skin tone
People & Body	👍🏽	thumbs up: medium skin tone
People & Body	👍🏾	thumbs up: medium-dark skin tone
People & Body	👍🏿	thumbs up: dark skin tone
People & Body	👎	thumbs down
People & Body	👎🏻	thumbs down: light skin tone
People & Body	👎🏼	thumbs down: medium-light skin tone
People & Body	👎🏽	thumbs down: medium skin tone
People & Body	👎🏾	thumbs down: medium-dark skin tone
People & Body	👎🏿	thumbs down: dark skin tone
People & Body	✊	raised fist
People & Body	✊🏻	raised fist: light skin tone
People & Body	✊🏼	raised fist: medium-light skin tone
People & Body	✊🏽	raised fist: medium skin tone
People & Body	✊🏾	raised fist: medium-dark skin tone
People & Body	✊🏿	raised fist: dark skin tone
People & Body	👊	oncoming fist
People & Body	👊🏻	oncoming fist: light skin tone
People & Body	👊🏼	oncoming fist: medium-light skin tone
People & Body	👊🏽	oncoming fist: medium skin tone
People & Body	👊🏾	oncoming fist: medium-dark skin tone
People & Body	👊🏿	oncoming fist: dark skin tone
People & Body	🤛	left-facing fist
People & Body	🤛🏻	left-facing fist: light skin tone
People & Body	🤛🏼	left-facing fist: medium-light skin tone
People & Body	🤛🏽	left-facing fist: medium skin tone
People & Body	🤛🏾	left-facing fist: medium-dark skin tone
People & Body	🤛🏿	left-facing fist: dark skin tone
People & Body	🤜	right-facing fist
People & Body	🤜🏻	right-facing fist: light skin tone
        """,
        """
People & Body	🤜🏼	right-facing fist: medium-light skin tone
People & Body	🤜🏽	right-facing fist: medium skin tone
People & Body	🤜🏾	right-facing fist: medium-dark skin tone
People & Body	🤜🏿	right-facing fist: dark skin tone
People & Body	👏	clapping hands
People & Body	👏🏻	clapping hands: light skin tone
People & Body	👏🏼	clapping hands: medium-light skin tone
People & Body	👏🏽	clapping hands: medium skin tone
People & Body	👏🏾	clapping hands: medium-dark skin tone
People & Body	👏🏿	clapping hands: dark skin tone
People & Body	🙌	raising hands
People & Body	🙌🏻	raising hands: light skin tone
People & Body	🙌🏼	raising hands: medium-light skin tone
People & Body	🙌🏽	raising hands: medium skin tone
People & Body	🙌🏾	raising hands: medium-dark skin tone
People & Body	🙌🏿	raising hands: dark skin tone
People & Body	🫶	heart hands
People & Body	🫶🏻	heart hands: light skin tone
People & Body	🫶🏼	heart hands: medium-light skin tone
People & Body	🫶🏽	heart hands: medium skin tone
People & Body	🫶🏾	heart hands: medium-dark skin tone
People & Body	🫶🏿	heart hands: dark skin tone
People & Body	👐	open hands
People & Body	👐🏻	open hands: light skin tone
People & Body	👐🏼	open hands: medium-light skin tone
People & Body	👐🏽	open hands: medium skin tone
People & Body	👐🏾	open hands: medium-dark skin tone
People & Body	👐🏿	open hands: dark skin tone
People & Body	🤲	palms up together
People & Body	🤲🏻	palms up together: light skin tone
People & Body	🤲🏼	palms up together: medium-light skin tone
People & Body	🤲🏽	palms up together: medium skin tone
People & Body	🤲🏾	palms up together: medium-dark skin tone
People & Body	🤲🏿	palms up together: dark skin tone
People & Body	🤝	handshake
People & Body	🤝🏻	handshake: light skin tone
People & Body	🤝🏼	handshake: medium-light skin tone
People & Body	🤝🏽	handshake: medium skin tone
People & Body	🤝🏾	handshake: medium-dark skin tone
People & Body	🤝🏿	handshake: dark skin tone
People & Body	🫱🏻‍🫲🏼	handshake: light skin tone, medium-light skin tone
People & Body	🫱🏻‍🫲🏽	handshake: light skin tone, medium skin tone
People & Body	🫱🏻‍🫲🏾	handshake: light skin tone, medium-dark skin tone
People & Body	🫱🏻‍🫲🏿	handshake: light skin tone, dark skin tone
People & Body	🫱🏼‍🫲🏻	handshake: medium-light skin tone, light skin tone
People & Body	🫱🏼‍🫲🏽	handshake: medium-light skin tone, medium skin tone
People & Body	🫱🏼‍🫲🏾	handshake: medium-light skin tone, medium-dark skin tone
People & Body	🫱🏼‍🫲🏿	handshake: medium-light skin tone, dark skin tone
People & Body	🫱🏽‍🫲🏻	handshake: medium skin tone, light skin tone
People & Body	🫱🏽‍🫲🏼	handshake: medium skin tone, medium-light skin tone
People & Body	🫱🏽‍🫲🏾	handshake: medium skin tone, medium-dark skin tone
People & Body	🫱🏽‍🫲🏿	handshake: medium skin tone, dark skin tone
People & Body	🫱🏾‍🫲🏻	handshake: medium-dark skin tone, light skin tone
People & Body	🫱🏾‍🫲🏼	handshake: medium-dark skin tone, medium-light skin tone
People & Body	🫱🏾‍🫲🏽	handshake: medium-dark skin tone, medium skin tone
People & Body	🫱🏾‍🫲🏿	handshake: medium-dark skin tone, dark skin tone
People & Body	🫱🏿‍🫲🏻	handshake: dark skin tone, light skin tone
People & Body	🫱🏿‍🫲🏼	handshake: dark skin tone, medium-light skin tone
People & Body	🫱🏿‍🫲🏽	handshake: dark skin tone, medium skin tone
People & Body	🫱🏿‍🫲🏾	handshake: dark skin tone, medium-dark skin tone
People & Body	🙏	folded hands
People & Body	🙏🏻	folded hands: light skin tone
People & Body	🙏🏼	folded hands: medium-light skin tone
People & Body	🙏🏽	folded hands: medium skin tone
People & Body	🙏🏾	folded hands: medium-dark skin tone
People & Body	🙏🏿	folded hands: dark skin tone
People & Body	✍️	writing hand
People & Body	✍🏻	writing hand: light skin tone
People & Body	✍🏼	writing hand: medium-light skin tone
People & Body	✍🏽	writing hand: medium skin tone
People & Body	✍🏾	writing hand: medium-dark skin tone
People & Body	✍🏿	writing hand: dark skin tone
People & Body	💅	nail polish
People & Body	💅🏻	nail polish: light skin tone
People & Body	💅🏼	nail polish: medium-light skin tone
People & Body	💅🏽	nail polish: medium skin tone
People & Body	💅🏾	nail polish: medium-dark skin tone
People & Body	💅🏿	nail polish: dark skin tone
People & Body	🤳	selfie
People & Body	🤳🏻	selfie: light skin tone
People & Body	🤳🏼	selfie: medium-light skin tone
People & Body	🤳🏽	selfie: medium skin tone
People & Body	🤳🏾	selfie: medium-dark skin tone
People & Body	🤳🏿	selfie: dark skin tone
People & Body	💪	flexed biceps
People & Body	💪🏻	flexed biceps: light skin tone
People & Body	💪🏼	flexed biceps: medium-light skin tone
People & Body	💪🏽	flexed biceps: medium skin tone
People & Body	💪🏾	flexed biceps: medium-dark skin tone
People & Body	💪🏿	flexed biceps: dark skin tone
People & Body	🦾	mechanical arm
People & Body	🦿	mechanical leg
People & Body	🦵	leg
People & Body	🦵🏻	leg: light skin tone
People & Body	🦵🏼	leg: medium-light skin tone
People & Body	🦵🏽	leg: medium skin tone
People & Body	🦵🏾	leg: medium-dark skin tone
People & Body	🦵🏿	leg: dark skin tone
People & Body	🦶	foot
People & Body	🦶🏻	foot: light skin tone
People & Body	🦶🏼	foot: medium-light skin tone
People & Body	🦶🏽	foot: medium skin tone
People & Body	🦶🏾	foot: medium-dark skin tone
People & Body	🦶🏿	foot: dark skin tone
People & Body	👂	ear
People & Body	👂🏻	ear: light skin tone
People & Body	👂🏼	ear: medium-light skin tone
People & Body	👂🏽	ear: medium skin tone
People & Body	👂🏾	ear: medium-dark skin tone
People & Body	👂🏿	ear: dark skin tone
People & Body	🦻	ear with hearing aid
People & Body	🦻🏻	ear with hearing aid: light skin tone
People & Body	🦻🏼	ear with hearing aid: medium-light skin tone
People & Body	🦻🏽	ear with hearing aid: medium skin tone
People & Body	🦻🏾	ear with hearing aid: medium-dark skin tone
People & Body	🦻🏿	ear with hearing aid: dark skin tone
People & Body	👃	nose
People & Body	👃🏻	nose: light skin tone
People & Body	👃🏼	nose: medium-light skin tone
People & Body	👃🏽	nose: medium skin tone
People & Body	👃🏾	nose: medium-dark skin tone
People & Body	👃🏿	nose: dark skin tone
People & Body	🧠	brain
People & Body	🫀	anatomical heart
People & Body	🫁	lungs
People & Body	🦷	tooth
People & Body	🦴	bone
People & Body	👀	eyes
People & Body	👁️	eye
People & Body	👅	tongue
People & Body	👄	mouth
People & Body	🫦	biting lip
People & Body	👶	baby
People & Body	👶🏻	baby: light skin tone
People & Body	👶🏼	baby: medium-light skin tone
People & Body	👶🏽	baby: medium skin tone
People & Body	👶🏾	baby: medium-dark skin tone
People & Body	👶🏿	baby: dark skin tone
People & Body	🧒	child
People & Body	🧒🏻	child: light skin tone
People & Body	🧒🏼	child: medium-light skin tone
People & Body	🧒🏽	child: medium skin tone
People & Body	🧒🏾	child: medium-dark skin tone
People & Body	🧒🏿	child: dark skin tone
People & Body	👦	boy
People & Body	👦🏻	boy: light skin tone
People & Body	👦🏼	boy: medium-light skin tone
People & Body	👦🏽	boy: medium skin tone
People & Body	👦🏾	boy: medium-dark skin tone
People & Body	👦🏿	boy: dark skin tone
People & Body	👧	girl
People & Body	👧🏻	girl: light skin tone
People & Body	👧🏼	girl: medium-light skin tone
People & Body	👧🏽	girl: medium skin tone
People & Body	👧🏾	girl: medium-dark skin tone
People & Body	👧🏿	girl: dark skin tone
People & Body	🧑	person
People & Body	🧑🏻	person: light skin tone
People & Body	🧑🏼	person: medium-light skin tone
People & Body	🧑🏽	person: medium skin tone
People & Body	🧑🏾	person: medium-dark skin tone
People & Body	🧑🏿	person: dark skin tone
People & Body	👱	person: blond hair
People & Body	👱🏻	person: light skin tone, blond hair
People & Body	👱🏼	person: medium-light skin tone, blond hair
People & Body	👱🏽	person: medium skin tone, blond hair
People & Body	👱🏾	person: medium-dark skin tone, blond hair
People & Body	👱🏿	person: dark skin tone, blond hair
People & Body	👨	man
People & Body	👨🏻	man: light skin tone
People & Body	👨🏼	man: medium-light skin tone
People & Body	👨🏽	man: medium skin tone
People & Body	👨🏾	man: medium-dark skin tone
People & Body	👨🏿	man: dark skin tone
People & Body	🧔	person: beard
People & Body	🧔🏻	person: light skin tone, beard
People & Body	🧔🏼	person: medium-light skin tone, beard
People & Body	🧔🏽	person: medium skin tone, beard
People & Body	🧔🏾	person: medium-dark skin tone, beard
People & Body	🧔🏿	person: dark skin tone, beard
People & Body	🧔‍♂️	man: beard
People & Body	🧔🏻‍♂️	man: light skin tone, beard
People & Body	🧔🏼‍♂️	man: medium-light skin tone, beard
People & Body	🧔🏽‍♂️	man: medium skin tone, beard
People & Body	🧔🏾‍♂️	man: medium-dark skin tone, beard
People & Body	🧔🏿‍♂️	man: dark skin tone, beard
People & Body	🧔‍♀️	woman: beard
People & Body	🧔🏻‍♀️	woman: light skin tone, beard
People & Body	🧔🏼‍♀️	woman: medium-light skin tone, beard
People & Body	🧔🏽‍♀️	woman: medium skin tone, beard
People & Body	🧔🏾‍♀️	woman: medium-dark skin tone, beard
People & Body	🧔🏿‍♀️	woman: dark skin tone, beard
People & Body	👨‍🦰	man: red hair
People & Body	👨🏻‍🦰	man: light skin tone, red hair
People & Body	👨🏼‍🦰	man: medium-light skin tone, red hair
People & Body	👨🏽‍🦰	man: medium skin tone, red hair
People & Body	👨🏾‍🦰	man: medium-dark skin tone, red hair
People & Body	👨🏿‍🦰	man: dark skin tone, red hair
People & Body	👨‍🦱	man: curly hair
People & Body	👨🏻‍🦱	man: light skin tone, curly hair
People & Body	👨🏼‍🦱	man: medium-light skin tone, curly hair
People & Body	👨🏽‍🦱	man: medium skin tone, curly hair
People & Body	👨🏾‍🦱	man: medium-dark skin tone, curly hair
People & Body	👨🏿‍🦱	man: dark skin tone, curly hair
People & Body	👨‍🦳	man: white hair
People & Body	👨🏻‍🦳	man: light skin tone, white hair
People & Body	👨🏼‍🦳	man: medium-light skin tone, white hair
People & Body	👨🏽‍🦳	man: medium skin tone, white hair
People & Body	👨🏾‍🦳	man: medium-dark skin tone, white hair
People & Body	👨🏿‍🦳	man: dark skin tone, white hair
People & Body	👨‍🦲	man: bald
People & Body	👨🏻‍🦲	man: light skin tone, bald
People & Body	👨🏼‍🦲	man: medium-light skin tone, bald
People & Body	👨🏽‍🦲	man: medium skin tone, bald
People & Body	👨🏾‍🦲	man: medium-dark skin tone, bald
People & Body	👨🏿‍🦲	man: dark skin tone, bald
People & Body	👩	woman
People & Body	👩🏻	woman: light skin tone
People & Body	👩🏼	woman: medium-light skin tone
People & Body	👩🏽	woman: medium skin tone
People & Body	👩🏾	woman: medium-dark skin tone
People & Body	👩🏿	woman: dark skin tone
People & Body	👩‍🦰	woman: red hair
People & Body	👩🏻‍🦰	woman: light skin tone, red hair
People & Body	👩🏼‍🦰	woman: medium-light skin tone, red hair
People & Body	👩🏽‍🦰	woman: medium skin tone, red hair
People & Body	👩🏾‍🦰	woman: medium-dark skin tone, red hair
People & Body	👩🏿‍🦰	woman: dark skin tone, red hair
People & Body	🧑‍🦰	person: red hair
People & Body	🧑🏻‍🦰	person: light skin tone, red hair
People & Body	🧑🏼‍🦰	person: medium-light skin tone, red hair
People & Body	🧑🏽‍🦰	person: medium skin tone, red hair
People & Body	🧑🏾‍🦰	person: medium-dark skin tone, red hair
People & Body	🧑🏿‍🦰	person: dark skin tone, red hair
People & Body	👩‍🦱	woman: curly hair
People & Body	👩🏻‍🦱	woman: light skin tone, curly hair
People & Body	👩🏼‍🦱	woman: medium-light skin tone, curly hair
People & Body	👩🏽‍🦱	woman: medium skin tone, curly hair
People & Body	👩🏾‍🦱	woman: medium-dark skin tone, curly hair
People & Body	👩🏿‍🦱	woman: dark skin tone, curly hair
People & Body	🧑‍🦱	person: curly hair
People & Body	🧑🏻‍🦱	person: light skin tone, curly hair
People & Body	🧑🏼‍🦱	person: medium-light skin tone, curly hair
People & Body	🧑🏽‍🦱	person: medium skin tone, curly hair
People & Body	🧑🏾‍🦱	person: medium-dark skin tone, curly hair
People & Body	🧑🏿‍🦱	person: dark skin tone, curly hair
People & Body	👩‍🦳	woman: white hair
People & Body	👩🏻‍🦳	woman: light skin tone, white hair
People & Body	👩🏼‍🦳	woman: medium-light skin tone, white hair
People & Body	👩🏽‍🦳	woman: medium skin tone, white hair
People & Body	👩🏾‍🦳	woman: medium-dark skin tone, white hair
People & Body	👩🏿‍🦳	woman: dark skin tone, white hair
People & Body	🧑‍🦳	person: white hair
People & Body	🧑🏻‍🦳	person: light skin tone, white hair
People & Body	🧑🏼‍🦳	person: medium-light skin tone, white hair
People & Body	🧑🏽‍🦳	person: medium skin tone, white hair
People & Body	🧑🏾‍🦳	person: medium-dark skin tone, white hair
People & Body	🧑🏿‍🦳	person: dark skin tone, white hair
People & Body	👩‍🦲	woman: bald
People & Body	👩🏻‍🦲	woman: light skin tone, bald
People & Body	👩🏼‍🦲	woman: medium-light skin tone, bald
People & Body	👩🏽‍🦲	woman: medium skin tone, bald
People & Body	👩🏾‍🦲	woman: medium-dark skin tone, bald
People & Body	👩🏿‍🦲	woman: dark skin tone, bald
People & Body	🧑‍🦲	person: bald
People & Body	🧑🏻‍🦲	person: light skin tone, bald
People & Body	🧑🏼‍🦲	person: medium-light skin tone, bald
People & Body	🧑🏽‍🦲	person: medium skin tone, bald
People & Body	🧑🏾‍🦲	person: medium-dark skin tone, bald
People & Body	🧑🏿‍🦲	person: dark skin tone, bald
People & Body	👱‍♀️	woman: blond hair
People & Body	👱🏻‍♀️	woman: light skin tone, blond hair
People & Body	👱🏼‍♀️	woman: medium-light skin tone, blond hair
People & Body	👱🏽‍♀️	woman: medium skin tone, blond hair
People & Body	👱🏾‍♀️	woman: medium-dark skin tone, blond hair
People & Body	👱🏿‍♀️	woman: dark skin tone, blond hair
People & Body	👱‍♂️	man: blond hair
People & Body	👱🏻‍♂️	man: light skin tone, blond hair
People & Body	👱🏼‍♂️	man: medium-light skin tone, blond hair
People & Body	👱🏽‍♂️	man: medium skin tone, blond hair
People & Body	👱🏾‍♂️	man: medium-dark skin tone, blond hair
People & Body	👱🏿‍♂️	man: dark skin tone, blond hair
People & Body	🧓	older person
People & Body	🧓🏻	older person: light skin tone
People & Body	🧓🏼	older person: medium-light skin tone
People & Body	🧓🏽	older person: medium skin tone
People & Body	🧓🏾	older person: medium-dark skin tone
People & Body	🧓🏿	older person: dark skin tone
People & Body	👴	old man
People & Body	👴🏻	old man: light skin tone
People & Body	👴🏼	old man: medium-light skin tone
People & Body	👴🏽	old man: medium skin tone
People & Body	👴🏾	old man: medium-dark skin tone
People & Body	👴🏿	old man: dark skin tone
People & Body	👵	old woman
People & Body	👵🏻	old woman: light skin tone
People & Body	👵🏼	old woman: medium-light skin tone
People & Body	👵🏽	old woman: medium skin tone
People & Body	👵🏾	old woman: medium-dark skin tone
People & Body	👵🏿	old woman: dark skin tone
People & Body	🙍	person frowning
People & Body	🙍🏻	person frowning: light skin tone
People & Body	🙍🏼	person frowning: medium-light skin tone
People & Body	🙍🏽	person frowning: medium skin tone
People & Body	🙍🏾	person frowning: medium-dark skin tone
People & Body	🙍🏿	person frowning: dark skin tone
People & Body	🙍‍♂️	man frowning
People & Body	🙍🏻‍♂️	man frowning: light skin tone
People & Body	🙍🏼‍♂️	man frowning: medium-light skin tone
People & Body	🙍🏽‍♂️	man frowning: medium skin tone
People & Body	🙍🏾‍♂️	man frowning: medium-dark skin tone
People & Body	🙍🏿‍♂️	man frowning: dark skin tone
People & Body	🙍‍♀️	woman frowning
People & Body	🙍🏻‍♀️	woman frowning: light skin tone
People & Body	🙍🏼‍♀️	woman frowning: medium-light skin tone
People & Body	🙍🏽‍♀️	woman frowning: medium skin tone
People & Body	🙍🏾‍♀️	woman frowning: medium-dark skin tone
People & Body	🙍🏿‍♀️	woman frowning: dark skin tone
People & Body	🙎	person pouting
People & Body	🙎🏻	person pouting: light skin tone
People & Body	🙎🏼	person pouting: medium-light skin tone
People & Body	🙎🏽	person pouting: medium skin tone
People & Body	🙎🏾	person pouting: medium-dark skin tone
People & Body	🙎🏿	person pouting: dark skin tone
People & Body	🙎‍♂️	man pouting
People & Body	🙎🏻‍♂️	man pouting: light skin tone
People & Body	🙎🏼‍♂️	man pouting: medium-light skin tone
People & Body	🙎🏽‍♂️	man pouting: medium skin tone
        """,
        """
People & Body	🙎🏾‍♂️	man pouting: medium-dark skin tone
People & Body	🙎🏿‍♂️	man pouting: dark skin tone
People & Body	🙎‍♀️	woman pouting
People & Body	🙎🏻‍♀️	woman pouting: light skin tone
People & Body	🙎🏼‍♀️	woman pouting: medium-light skin tone
People & Body	🙎🏽‍♀️	woman pouting: medium skin tone
People & Body	🙎🏾‍♀️	woman pouting: medium-dark skin tone
People & Body	🙎🏿‍♀️	woman pouting: dark skin tone
People & Body	🙅	person gesturing NO
People & Body	🙅🏻	person gesturing NO: light skin tone
People & Body	🙅🏼	person gesturing NO: medium-light skin tone
People & Body	🙅🏽	person gesturing NO: medium skin tone
People & Body	🙅🏾	person gesturing NO: medium-dark skin tone
People & Body	🙅🏿	person gesturing NO: dark skin tone
People & Body	🙅‍♂️	man gesturing NO
People & Body	🙅🏻‍♂️	man gesturing NO: light skin tone
People & Body	🙅🏼‍♂️	man gesturing NO: medium-light skin tone
People & Body	🙅🏽‍♂️	man gesturing NO: medium skin tone
People & Body	🙅🏾‍♂️	man gesturing NO: medium-dark skin tone
People & Body	🙅🏿‍♂️	man gesturing NO: dark skin tone
People & Body	🙅‍♀️	woman gesturing NO
People & Body	🙅🏻‍♀️	woman gesturing NO: light skin tone
People & Body	🙅🏼‍♀️	woman gesturing NO: medium-light skin tone
People & Body	🙅🏽‍♀️	woman gesturing NO: medium skin tone
People & Body	🙅🏾‍♀️	woman gesturing NO: medium-dark skin tone
People & Body	🙅🏿‍♀️	woman gesturing NO: dark skin tone
People & Body	🙆	person gesturing OK
People & Body	🙆🏻	person gesturing OK: light skin tone
People & Body	🙆🏼	person gesturing OK: medium-light skin tone
People & Body	🙆🏽	person gesturing OK: medium skin tone
People & Body	🙆🏾	person gesturing OK: medium-dark skin tone
People & Body	🙆🏿	person gesturing OK: dark skin tone
People & Body	🙆‍♂️	man gesturing OK
People & Body	🙆🏻‍♂️	man gesturing OK: light skin tone
People & Body	🙆🏼‍♂️	man gesturing OK: medium-light skin tone
People & Body	🙆🏽‍♂️	man gesturing OK: medium skin tone
People & Body	🙆🏾‍♂️	man gesturing OK: medium-dark skin tone
People & Body	🙆🏿‍♂️	man gesturing OK: dark skin tone
People & Body	🙆‍♀️	woman gesturing OK
People & Body	🙆🏻‍♀️	woman gesturing OK: light skin tone
People & Body	🙆🏼‍♀️	woman gesturing OK: medium-light skin tone
People & Body	🙆🏽‍♀️	woman gesturing OK: medium skin tone
People & Body	🙆🏾‍♀️	woman gesturing OK: medium-dark skin tone
People & Body	🙆🏿‍♀️	woman gesturing OK: dark skin tone
People & Body	💁	person tipping hand
People & Body	💁🏻	person tipping hand: light skin tone
People & Body	💁🏼	person tipping hand: medium-light skin tone
People & Body	💁🏽	person tipping hand: medium skin tone
People & Body	💁🏾	person tipping hand: medium-dark skin tone
People & Body	💁🏿	person tipping hand: dark skin tone
People & Body	💁‍♂️	man tipping hand
People & Body	💁🏻‍♂️	man tipping hand: light skin tone
People & Body	💁🏼‍♂️	man tipping hand: medium-light skin tone
People & Body	💁🏽‍♂️	man tipping hand: medium skin tone
People & Body	💁🏾‍♂️	man tipping hand: medium-dark skin tone
People & Body	💁🏿‍♂️	man tipping hand: dark skin tone
People & Body	💁‍♀️	woman tipping hand
People & Body	💁🏻‍♀️	woman tipping hand: light skin tone
People & Body	💁🏼‍♀️	woman tipping hand: medium-light skin tone
People & Body	💁🏽‍♀️	woman tipping hand: medium skin tone
People & Body	💁🏾‍♀️	woman tipping hand: medium-dark skin tone
People & Body	💁🏿‍♀️	woman tipping hand: dark skin tone
People & Body	🙋	person raising hand
People & Body	🙋🏻	person raising hand: light skin tone
People & Body	🙋🏼	person raising hand: medium-light skin tone
People & Body	🙋🏽	person raising hand: medium skin tone
People & Body	🙋🏾	person raising hand: medium-dark skin tone
People & Body	🙋🏿	person raising hand: dark skin tone
People & Body	🙋‍♂️	man raising hand
People & Body	🙋🏻‍♂️	man raising hand: light skin tone
People & Body	🙋🏼‍♂️	man raising hand: medium-light skin tone
People & Body	🙋🏽‍♂️	man raising hand: medium skin tone
People & Body	🙋🏾‍♂️	man raising hand: medium-dark skin tone
People & Body	🙋🏿‍♂️	man raising hand: dark skin tone
People & Body	🙋‍♀️	woman raising hand
People & Body	🙋🏻‍♀️	woman raising hand: light skin tone
People & Body	🙋🏼‍♀️	woman raising hand: medium-light skin tone
People & Body	🙋🏽‍♀️	woman raising hand: medium skin tone
People & Body	🙋🏾‍♀️	woman raising hand: medium-dark skin tone
People & Body	🙋🏿‍♀️	woman raising hand: dark skin tone
People & Body	🧏	deaf person
People & Body	🧏🏻	deaf person: light skin tone
People & Body	🧏🏼	deaf person: medium-light skin tone
People & Body	🧏🏽	deaf person: medium skin tone
People & Body	🧏🏾	deaf person: medium-dark skin tone
People & Body	🧏🏿	deaf person: dark skin tone
People & Body	🧏‍♂️	deaf man
People & Body	🧏🏻‍♂️	deaf man: light skin tone
People & Body	🧏🏼‍♂️	deaf man: medium-light skin tone
People & Body	🧏🏽‍♂️	deaf man: medium skin tone
People & Body	🧏🏾‍♂️	deaf man: medium-dark skin tone
People & Body	🧏🏿‍♂️	deaf man: dark skin tone
People & Body	🧏‍♀️	deaf woman
People & Body	🧏🏻‍♀️	deaf woman: light skin tone
People & Body	🧏🏼‍♀️	deaf woman: medium-light skin tone
People & Body	🧏🏽‍♀️	deaf woman: medium skin tone
People & Body	🧏🏾‍♀️	deaf woman: medium-dark skin tone
People & Body	🧏🏿‍♀️	deaf woman: dark skin tone
People & Body	🙇	person bowing
People & Body	🙇🏻	person bowing: light skin tone
People & Body	🙇🏼	person bowing: medium-light skin tone
People & Body	🙇🏽	person bowing: medium skin tone
People & Body	🙇🏾	person bowing: medium-dark skin tone
People & Body	🙇🏿	person bowing: dark skin tone
People & Body	🙇‍♂️	man bowing
People & Body	🙇🏻‍♂️	man bowing: light skin tone
People & Body	🙇🏼‍♂️	man bowing: medium-light skin tone
People & Body	🙇🏽‍♂️	man bowing: medium skin tone
People & Body	🙇🏾‍♂️	man bowing: medium-dark skin tone
People & Body	🙇🏿‍♂️	man bowing: dark skin tone
People & Body	🙇‍♀️	woman bowing
People & Body	🙇🏻‍♀️	woman bowing: light skin tone
People & Body	🙇🏼‍♀️	woman bowing: medium-light skin tone
People & Body	🙇🏽‍♀️	woman bowing: medium skin tone
People & Body	🙇🏾‍♀️	woman bowing: medium-dark skin tone
People & Body	🙇🏿‍♀️	woman bowing: dark skin tone
People & Body	🤦	person facepalming
People & Body	🤦🏻	person facepalming: light skin tone
People & Body	🤦🏼	person facepalming: medium-light skin tone
People & Body	🤦🏽	person facepalming: medium skin tone
People & Body	🤦🏾	person facepalming: medium-dark skin tone
People & Body	🤦🏿	person facepalming: dark skin tone
People & Body	🤦‍♂️	man facepalming
People & Body	🤦🏻‍♂️	man facepalming: light skin tone
People & Body	🤦🏼‍♂️	man facepalming: medium-light skin tone
People & Body	🤦🏽‍♂️	man facepalming: medium skin tone
People & Body	🤦🏾‍♂️	man facepalming: medium-dark skin tone
People & Body	🤦🏿‍♂️	man facepalming: dark skin tone
People & Body	🤦‍♀️	woman facepalming
People & Body	🤦🏻‍♀️	woman facepalming: light skin tone
People & Body	🤦🏼‍♀️	woman facepalming: medium-light skin tone
People & Body	🤦🏽‍♀️	woman facepalming: medium skin tone
People & Body	🤦🏾‍♀️	woman facepalming: medium-dark skin tone
People & Body	🤦🏿‍♀️	woman facepalming: dark skin tone
People & Body	🤷	person shrugging
People & Body	🤷🏻	person shrugging: light skin tone
People & Body	🤷🏼	person shrugging: medium-light skin tone
People & Body	🤷🏽	person shrugging: medium skin tone
People & Body	🤷🏾	person shrugging: medium-dark skin tone
People & Body	🤷🏿	person shrugging: dark skin tone
People & Body	🤷‍♂️	man shrugging
People & Body	🤷🏻‍♂️	man shrugging: light skin tone
People & Body	🤷🏼‍♂️	man shrugging: medium-light skin tone
People & Body	🤷🏽‍♂️	man shrugging: medium skin tone
People & Body	🤷🏾‍♂️	man shrugging: medium-dark skin tone
People & Body	🤷🏿‍♂️	man shrugging: dark skin tone
People & Body	🤷‍♀️	woman shrugging
People & Body	🤷🏻‍♀️	woman shrugging: light skin tone
People & Body	🤷🏼‍♀️	woman shrugging: medium-light skin tone
People & Body	🤷🏽‍♀️	woman shrugging: medium skin tone
People & Body	🤷🏾‍♀️	woman shrugging: medium-dark skin tone
People & Body	🤷🏿‍♀️	woman shrugging: dark skin tone
People & Body	🧑‍⚕️	health worker
People & Body	🧑🏻‍⚕️	health worker: light skin tone
People & Body	🧑🏼‍⚕️	health worker: medium-light skin tone
People & Body	🧑🏽‍⚕️	health worker: medium skin tone
People & Body	🧑🏾‍⚕️	health worker: medium-dark skin tone
People & Body	🧑🏿‍⚕️	health worker: dark skin tone
People & Body	👨‍⚕️	man health worker
People & Body	👨🏻‍⚕️	man health worker: light skin tone
People & Body	👨🏼‍⚕️	man health worker: medium-light skin tone
People & Body	👨🏽‍⚕️	man health worker: medium skin tone
People & Body	👨🏾‍⚕️	man health worker: medium-dark skin tone
People & Body	👨🏿‍⚕️	man health worker: dark skin tone
People & Body	👩‍⚕️	woman health worker
People & Body	👩🏻‍⚕️	woman health worker: light skin tone
People & Body	👩🏼‍⚕️	woman health worker: medium-light skin tone
People & Body	👩🏽‍⚕️	woman health worker: medium skin tone
People & Body	👩🏾‍⚕️	woman health worker: medium-dark skin tone
People & Body	👩🏿‍⚕️	woman health worker: dark skin tone
People & Body	🧑‍🎓	student
People & Body	🧑🏻‍🎓	student: light skin tone
People & Body	🧑🏼‍🎓	student: medium-light skin tone
People & Body	🧑🏽‍🎓	student: medium skin tone
People & Body	🧑🏾‍🎓	student: medium-dark skin tone
People & Body	🧑🏿‍🎓	student: dark skin tone
People & Body	👨‍🎓	man student
People & Body	👨🏻‍🎓	man student: light skin tone
People & Body	👨🏼‍🎓	man student: medium-light skin tone
People & Body	👨🏽‍🎓	man student: medium skin tone
People & Body	👨🏾‍🎓	man student: medium-dark skin tone
People & Body	👨🏿‍🎓	man student: dark skin tone
People & Body	👩‍🎓	woman student
People & Body	👩🏻‍🎓	woman student: light skin tone
People & Body	👩🏼‍🎓	woman student: medium-light skin tone
People & Body	👩🏽‍🎓	woman student: medium skin tone
People & Body	👩🏾‍🎓	woman student: medium-dark skin tone
People & Body	👩🏿‍🎓	woman student: dark skin tone
People & Body	🧑‍🏫	teacher
People & Body	🧑🏻‍🏫	teacher: light skin tone
People & Body	🧑🏼‍🏫	teacher: medium-light skin tone
People & Body	🧑🏽‍🏫	teacher: medium skin tone
People & Body	🧑🏾‍🏫	teacher: medium-dark skin tone
People & Body	🧑🏿‍🏫	teacher: dark skin tone
People & Body	👨‍🏫	man teacher
People & Body	👨🏻‍🏫	man teacher: light skin tone
People & Body	👨🏼‍🏫	man teacher: medium-light skin tone
People & Body	👨🏽‍🏫	man teacher: medium skin tone
People & Body	👨🏾‍🏫	man teacher: medium-dark skin tone
People & Body	👨🏿‍🏫	man teacher: dark skin tone
People & Body	👩‍🏫	woman teacher
People & Body	👩🏻‍🏫	woman teacher: light skin tone
People & Body	👩🏼‍🏫	woman teacher: medium-light skin tone
People & Body	👩🏽‍🏫	woman teacher: medium skin tone
People & Body	👩🏾‍🏫	woman teacher: medium-dark skin tone
People & Body	👩🏿‍🏫	woman teacher: dark skin tone
People & Body	🧑‍⚖️	judge
People & Body	🧑🏻‍⚖️	judge: light skin tone
People & Body	🧑🏼‍⚖️	judge: medium-light skin tone
People & Body	🧑🏽‍⚖️	judge: medium skin tone
People & Body	🧑🏾‍⚖️	judge: medium-dark skin tone
People & Body	🧑🏿‍⚖️	judge: dark skin tone
People & Body	👨‍⚖️	man judge
People & Body	👨🏻‍⚖️	man judge: light skin tone
People & Body	👨🏼‍⚖️	man judge: medium-light skin tone
People & Body	👨🏽‍⚖️	man judge: medium skin tone
People & Body	👨🏾‍⚖️	man judge: medium-dark skin tone
People & Body	👨🏿‍⚖️	man judge: dark skin tone
People & Body	👩‍⚖️	woman judge
People & Body	👩🏻‍⚖️	woman judge: light skin tone
People & Body	👩🏼‍⚖️	woman judge: medium-light skin tone
People & Body	👩🏽‍⚖️	woman judge: medium skin tone
People & Body	👩🏾‍⚖️	woman judge: medium-dark skin tone
People & Body	👩🏿‍⚖️	woman judge: dark skin tone
People & Body	🧑‍🌾	farmer
People & Body	🧑🏻‍🌾	farmer: light skin tone
People & Body	🧑🏼‍🌾	farmer: medium-light skin tone
People & Body	🧑🏽‍🌾	farmer: medium skin tone
People & Body	🧑🏾‍🌾	farmer: medium-dark skin tone
People & Body	🧑🏿‍🌾	farmer: dark skin tone
People & Body	👨‍🌾	man farmer
People & Body	👨🏻‍🌾	man farmer: light skin tone
People & Body	👨🏼‍🌾	man farmer: medium-light skin tone
People & Body	👨🏽‍🌾	man farmer: medium skin tone
People & Body	👨🏾‍🌾	man farmer: medium-dark skin tone
People & Body	👨🏿‍🌾	man farmer: dark skin tone
People & Body	👩‍🌾	woman farmer
People & Body	👩🏻‍🌾	woman farmer: light skin tone
People & Body	👩🏼‍🌾	woman farmer: medium-light skin tone
People & Body	👩🏽‍🌾	woman farmer: medium skin tone
People & Body	👩🏾‍🌾	woman farmer: medium-dark skin tone
People & Body	👩🏿‍🌾	woman farmer: dark skin tone
People & Body	🧑‍🍳	cook
People & Body	🧑🏻‍🍳	cook: light skin tone
People & Body	🧑🏼‍🍳	cook: medium-light skin tone
People & Body	🧑🏽‍🍳	cook: medium skin tone
People & Body	🧑🏾‍🍳	cook: medium-dark skin tone
People & Body	🧑🏿‍🍳	cook: dark skin tone
People & Body	👨‍🍳	man cook
People & Body	👨🏻‍🍳	man cook: light skin tone
People & Body	👨🏼‍🍳	man cook: medium-light skin tone
People & Body	👨🏽‍🍳	man cook: medium skin tone
People & Body	👨🏾‍🍳	man cook: medium-dark skin tone
People & Body	👨🏿‍🍳	man cook: dark skin tone
People & Body	👩‍🍳	woman cook
People & Body	👩🏻‍🍳	woman cook: light skin tone
People & Body	👩🏼‍🍳	woman cook: medium-light skin tone
People & Body	👩🏽‍🍳	woman cook: medium skin tone
People & Body	👩🏾‍🍳	woman cook: medium-dark skin tone
People & Body	👩🏿‍🍳	woman cook: dark skin tone
People & Body	🧑‍🔧	mechanic
People & Body	🧑🏻‍🔧	mechanic: light skin tone
People & Body	🧑🏼‍🔧	mechanic: medium-light skin tone
People & Body	🧑🏽‍🔧	mechanic: medium skin tone
People & Body	🧑🏾‍🔧	mechanic: medium-dark skin tone
People & Body	🧑🏿‍🔧	mechanic: dark skin tone
People & Body	👨‍🔧	man mechanic
People & Body	👨🏻‍🔧	man mechanic: light skin tone
People & Body	👨🏼‍🔧	man mechanic: medium-light skin tone
People & Body	👨🏽‍🔧	man mechanic: medium skin tone
People & Body	👨🏾‍🔧	man mechanic: medium-dark skin tone
People & Body	👨🏿‍🔧	man mechanic: dark skin tone
People & Body	👩‍🔧	woman mechanic
People & Body	👩🏻‍🔧	woman mechanic: light skin tone
People & Body	👩🏼‍🔧	woman mechanic: medium-light skin tone
People & Body	👩🏽‍🔧	woman mechanic: medium skin tone
People & Body	👩🏾‍🔧	woman mechanic: medium-dark skin tone
People & Body	👩🏿‍🔧	woman mechanic: dark skin tone
People & Body	🧑‍🏭	factory worker
People & Body	🧑🏻‍🏭	factory worker: light skin tone
People & Body	🧑🏼‍🏭	factory worker: medium-light skin tone
People & Body	🧑🏽‍🏭	factory worker: medium skin tone
People & Body	🧑🏾‍🏭	factory worker: medium-dark skin tone
People & Body	🧑🏿‍🏭	factory worker: dark skin tone
People & Body	👨‍🏭	man factory worker
People & Body	👨🏻‍🏭	man factory worker: light skin tone
People & Body	👨🏼‍🏭	man factory worker: medium-light skin tone
People & Body	👨🏽‍🏭	man factory worker: medium skin tone
People & Body	👨🏾‍🏭	man factory worker: medium-dark skin tone
People & Body	👨🏿‍🏭	man factory worker: dark skin tone
People & Body	👩‍🏭	woman factory worker
People & Body	👩🏻‍🏭	woman factory worker: light skin tone
People & Body	👩🏼‍🏭	woman factory worker: medium-light skin tone
People & Body	👩🏽‍🏭	woman factory worker: medium skin tone
People & Body	👩🏾‍🏭	woman factory worker: medium-dark skin tone
People & Body	👩🏿‍🏭	woman factory worker: dark skin tone
People & Body	🧑‍💼	office worker
People & Body	🧑🏻‍💼	office worker: light skin tone
People & Body	🧑🏼‍💼	office worker: medium-light skin tone
People & Body	🧑🏽‍💼	office worker: medium skin tone
        """,
        """
People & Body	🧑🏾‍💼	office worker: medium-dark skin tone
People & Body	🧑🏿‍💼	office worker: dark skin tone
People & Body	👨‍💼	man office worker
People & Body	👨🏻‍💼	man office worker: light skin tone
People & Body	👨🏼‍💼	man office worker: medium-light skin tone
People & Body	👨🏽‍💼	man office worker: medium skin tone
People & Body	👨🏾‍💼	man office worker: medium-dark skin tone
People & Body	👨🏿‍💼	man office worker: dark skin tone
People & Body	👩‍💼	woman office worker
People & Body	👩🏻‍💼	woman office worker: light skin tone
People & Body	👩🏼‍💼	woman office worker: medium-light skin tone
People & Body	👩🏽‍💼	woman office worker: medium skin tone
People & Body	👩🏾‍💼	woman office worker: medium-dark skin tone
People & Body	👩🏿‍💼	woman office worker: dark skin tone
People & Body	🧑‍🔬	scientist
People & Body	🧑🏻‍🔬	scientist: light skin tone
People & Body	🧑🏼‍🔬	scientist: medium-light skin tone
People & Body	🧑🏽‍🔬	scientist: medium skin tone
People & Body	🧑🏾‍🔬	scientist: medium-dark skin tone
People & Body	🧑🏿‍🔬	scientist: dark skin tone
People & Body	👨‍🔬	man scientist
People & Body	👨🏻‍🔬	man scientist: light skin tone
People & Body	👨🏼‍🔬	man scientist: medium-light skin tone
People & Body	👨🏽‍🔬	man scientist: medium skin tone
People & Body	👨🏾‍🔬	man scientist: medium-dark skin tone
People & Body	👨🏿‍🔬	man scientist: dark skin tone
People & Body	👩‍🔬	woman scientist
People & Body	👩🏻‍🔬	woman scientist: light skin tone
People & Body	👩🏼‍🔬	woman scientist: medium-light skin tone
People & Body	👩🏽‍🔬	woman scientist: medium skin tone
People & Body	👩🏾‍🔬	woman scientist: medium-dark skin tone
People & Body	👩🏿‍🔬	woman scientist: dark skin tone
People & Body	🧑‍💻	technologist
People & Body	🧑🏻‍💻	technologist: light skin tone
People & Body	🧑🏼‍💻	technologist: medium-light skin tone
People & Body	🧑🏽‍💻	technologist: medium skin tone
People & Body	🧑🏾‍💻	technologist: medium-dark skin tone
People & Body	🧑🏿‍💻	technologist: dark skin tone
People & Body	👨‍💻	man technologist
People & Body	👨🏻‍💻	man technologist: light skin tone
People & Body	👨🏼‍💻	man technologist: medium-light skin tone
People & Body	👨🏽‍💻	man technologist: medium skin tone
People & Body	👨🏾‍💻	man technologist: medium-dark skin tone
People & Body	👨🏿‍💻	man technologist: dark skin tone
People & Body	👩‍💻	woman technologist
People & Body	👩🏻‍💻	woman technologist: light skin tone
People & Body	👩🏼‍💻	woman technologist: medium-light skin tone
People & Body	👩🏽‍💻	woman technologist: medium skin tone
People & Body	👩🏾‍💻	woman technologist: medium-dark skin tone
People & Body	👩🏿‍💻	woman technologist: dark skin tone
People & Body	🧑‍🎤	singer
People & Body	🧑🏻‍🎤	singer: light skin tone
People & Body	🧑🏼‍🎤	singer: medium-light skin tone
People & Body	🧑🏽‍🎤	singer: medium skin tone
People & Body	🧑🏾‍🎤	singer: medium-dark skin tone
People & Body	🧑🏿‍🎤	singer: dark skin tone
People & Body	👨‍🎤	man singer
People & Body	👨🏻‍🎤	man singer: light skin tone
People & Body	👨🏼‍🎤	man singer: medium-light skin tone
People & Body	👨🏽‍🎤	man singer: medium skin tone
People & Body	👨🏾‍🎤	man singer: medium-dark skin tone
People & Body	👨🏿‍🎤	man singer: dark skin tone
People & Body	👩‍🎤	woman singer
People & Body	👩🏻‍🎤	woman singer: light skin tone
People & Body	👩🏼‍🎤	woman singer: medium-light skin tone
People & Body	👩🏽‍🎤	woman singer: medium skin tone
People & Body	👩🏾‍🎤	woman singer: medium-dark skin tone
People & Body	👩🏿‍🎤	woman singer: dark skin tone
People & Body	🧑‍🎨	artist
People & Body	🧑🏻‍🎨	artist: light skin tone
People & Body	🧑🏼‍🎨	artist: medium-light skin tone
People & Body	🧑🏽‍🎨	artist: medium skin tone
People & Body	🧑🏾‍🎨	artist: medium-dark skin tone
People & Body	🧑🏿‍🎨	artist: dark skin tone
People & Body	👨‍🎨	man artist
People & Body	👨🏻‍🎨	man artist: light skin tone
People & Body	👨🏼‍🎨	man artist: medium-light skin tone
People & Body	👨🏽‍🎨	man artist: medium skin tone
People & Body	👨🏾‍🎨	man artist: medium-dark skin tone
People & Body	👨🏿‍🎨	man artist: dark skin tone
People & Body	👩‍🎨	woman artist
People & Body	👩🏻‍🎨	woman artist: light skin tone
People & Body	👩🏼‍🎨	woman artist: medium-light skin tone
People & Body	👩🏽‍🎨	woman artist: medium skin tone
People & Body	👩🏾‍🎨	woman artist: medium-dark skin tone
People & Body	👩🏿‍🎨	woman artist: dark skin tone
People & Body	🧑‍✈️	pilot
People & Body	🧑🏻‍✈️	pilot: light skin tone
People & Body	🧑🏼‍✈️	pilot: medium-light skin tone
People & Body	🧑🏽‍✈️	pilot: medium skin tone
People & Body	🧑🏾‍✈️	pilot: medium-dark skin tone
People & Body	🧑🏿‍✈️	pilot: dark skin tone
People & Body	👨‍✈️	man pilot
People & Body	👨🏻‍✈️	man pilot: light skin tone
People & Body	👨🏼‍✈️	man pilot: medium-light skin tone
People & Body	👨🏽‍✈️	man pilot: medium skin tone
People & Body	👨🏾‍✈️	man pilot: medium-dark skin tone
People & Body	👨🏿‍✈️	man pilot: dark skin tone
People & Body	👩‍✈️	woman pilot
People & Body	👩🏻‍✈️	woman pilot: light skin tone
People & Body	👩🏼‍✈️	woman pilot: medium-light skin tone
People & Body	👩🏽‍✈️	woman pilot: medium skin tone
People & Body	👩🏾‍✈️	woman pilot: medium-dark skin tone
People & Body	👩🏿‍✈️	woman pilot: dark skin tone
People & Body	🧑‍🚀	astronaut
People & Body	🧑🏻‍🚀	astronaut: light skin tone
People & Body	🧑🏼‍🚀	astronaut: medium-light skin tone
People & Body	🧑🏽‍🚀	astronaut: medium skin tone
People & Body	🧑🏾‍🚀	astronaut: medium-dark skin tone
People & Body	🧑🏿‍🚀	astronaut: dark skin tone
People & Body	👨‍🚀	man astronaut
People & Body	👨🏻‍🚀	man astronaut: light skin tone
People & Body	👨🏼‍🚀	man astronaut: medium-light skin tone
People & Body	👨🏽‍🚀	man astronaut: medium skin tone
People & Body	👨🏾‍🚀	man astronaut: medium-dark skin tone
People & Body	👨🏿‍🚀	man astronaut: dark skin tone
People & Body	👩‍🚀	woman astronaut
People & Body	👩🏻‍🚀	woman astronaut: light skin tone
People & Body	👩🏼‍🚀	woman astronaut: medium-light skin tone
People & Body	👩🏽‍🚀	woman astronaut: medium skin tone
People & Body	👩🏾‍🚀	woman astronaut: medium-dark skin tone
People & Body	👩🏿‍🚀	woman astronaut: dark skin tone
People & Body	🧑‍🚒	firefighter
People & Body	🧑🏻‍🚒	firefighter: light skin tone
People & Body	🧑🏼‍🚒	firefighter: medium-light skin tone
People & Body	🧑🏽‍🚒	firefighter: medium skin tone
People & Body	🧑🏾‍🚒	firefighter: medium-dark skin tone
People & Body	🧑🏿‍🚒	firefighter: dark skin tone
People & Body	👨‍🚒	man firefighter
People & Body	👨🏻‍🚒	man firefighter: light skin tone
People & Body	👨🏼‍🚒	man firefighter: medium-light skin tone
People & Body	👨🏽‍🚒	man firefighter: medium skin tone
People & Body	👨🏾‍🚒	man firefighter: medium-dark skin tone
People & Body	👨🏿‍🚒	man firefighter: dark skin tone
People & Body	👩‍🚒	woman firefighter
People & Body	👩🏻‍🚒	woman firefighter: light skin tone
People & Body	👩🏼‍🚒	woman firefighter: medium-light skin tone
People & Body	👩🏽‍🚒	woman firefighter: medium skin tone
People & Body	👩🏾‍🚒	woman firefighter: medium-dark skin tone
People & Body	👩🏿‍🚒	woman firefighter: dark skin tone
People & Body	👮	police officer
People & Body	👮🏻	police officer: light skin tone
People & Body	👮🏼	police officer: medium-light skin tone
People & Body	👮🏽	police officer: medium skin tone
People & Body	👮🏾	police officer: medium-dark skin tone
People & Body	👮🏿	police officer: dark skin tone
People & Body	👮‍♂️	man police officer
People & Body	👮🏻‍♂️	man police officer: light skin tone
People & Body	👮🏼‍♂️	man police officer: medium-light skin tone
People & Body	👮🏽‍♂️	man police officer: medium skin tone
People & Body	👮🏾‍♂️	man police officer: medium-dark skin tone
People & Body	👮🏿‍♂️	man police officer: dark skin tone
People & Body	👮‍♀️	woman police officer
People & Body	👮🏻‍♀️	woman police officer: light skin tone
People & Body	👮🏼‍♀️	woman police officer: medium-light skin tone
People & Body	👮🏽‍♀️	woman police officer: medium skin tone
People & Body	👮🏾‍♀️	woman police officer: medium-dark skin tone
People & Body	👮🏿‍♀️	woman police officer: dark skin tone
People & Body	🕵️	detective
People & Body	🕵🏻	detective: light skin tone
People & Body	🕵🏼	detective: medium-light skin tone
People & Body	🕵🏽	detective: medium skin tone
People & Body	🕵🏾	detective: medium-dark skin tone
People & Body	🕵🏿	detective: dark skin tone
People & Body	🕵️‍♂️	man detective
People & Body	🕵🏻‍♂️	man detective: light skin tone
People & Body	🕵🏼‍♂️	man detective: medium-light skin tone
People & Body	🕵🏽‍♂️	man detective: medium skin tone
People & Body	🕵🏾‍♂️	man detective: medium-dark skin tone
People & Body	🕵🏿‍♂️	man detective: dark skin tone
People & Body	🕵️‍♀️	woman detective
People & Body	🕵🏻‍♀️	woman detective: light skin tone
People & Body	🕵🏼‍♀️	woman detective: medium-light skin tone
People & Body	🕵🏽‍♀️	woman detective: medium skin tone
People & Body	🕵🏾‍♀️	woman detective: medium-dark skin tone
People & Body	🕵🏿‍♀️	woman detective: dark skin tone
People & Body	💂	guard
People & Body	💂🏻	guard: light skin tone
People & Body	💂🏼	guard: medium-light skin tone
People & Body	💂🏽	guard: medium skin tone
People & Body	💂🏾	guard: medium-dark skin tone
People & Body	💂🏿	guard: dark skin tone
People & Body	💂‍♂️	man guard
People & Body	💂🏻‍♂️	man guard: light skin tone
People & Body	💂🏼‍♂️	man guard: medium-light skin tone
People & Body	💂🏽‍♂️	man guard: medium skin tone
People & Body	💂🏾‍♂️	man guard: medium-dark skin tone
People & Body	💂🏿‍♂️	man guard: dark skin tone
People & Body	💂‍♀️	woman guard
People & Body	💂🏻‍♀️	woman guard: light skin tone
People & Body	💂🏼‍♀️	woman guard: medium-light skin tone
People & Body	💂🏽‍♀️	woman guard: medium skin tone
People & Body	💂🏾‍♀️	woman guard: medium-dark skin tone
People & Body	💂🏿‍♀️	woman guard: dark skin tone
People & Body	🥷	ninja
People & Body	🥷🏻	ninja: light skin tone
People & Body	🥷🏼	ninja: medium-light skin tone
People & Body	🥷🏽	ninja: medium skin tone
People & Body	🥷🏾	ninja: medium-dark skin tone
People & Body	🥷🏿	ninja: dark skin tone
People & Body	👷	construction worker
People & Body	👷🏻	construction worker: light skin tone
People & Body	👷🏼	construction worker: medium-light skin tone
People & Body	👷🏽	construction worker: medium skin tone
People & Body	👷🏾	construction worker: medium-dark skin tone
People & Body	👷🏿	construction worker: dark skin tone
People & Body	👷‍♂️	man construction worker
People & Body	👷🏻‍♂️	man construction worker: light skin tone
People & Body	👷🏼‍♂️	man construction worker: medium-light skin tone
People & Body	👷🏽‍♂️	man construction worker: medium skin tone
People & Body	👷🏾‍♂️	man construction worker: medium-dark skin tone
People & Body	👷🏿‍♂️	man construction worker: dark skin tone
People & Body	👷‍♀️	woman construction worker
People & Body	👷🏻‍♀️	woman construction worker: light skin tone
People & Body	👷🏼‍♀️	woman construction worker: medium-light skin tone
People & Body	👷🏽‍♀️	woman construction worker: medium skin tone
People & Body	👷🏾‍♀️	woman construction worker: medium-dark skin tone
People & Body	👷🏿‍♀️	woman construction worker: dark skin tone
People & Body	🫅	person with crown
People & Body	🫅🏻	person with crown: light skin tone
People & Body	🫅🏼	person with crown: medium-light skin tone
People & Body	🫅🏽	person with crown: medium skin tone
People & Body	🫅🏾	person with crown: medium-dark skin tone
People & Body	🫅🏿	person with crown: dark skin tone
People & Body	🤴	prince
People & Body	🤴🏻	prince: light skin tone
People & Body	🤴🏼	prince: medium-light skin tone
People & Body	🤴🏽	prince: medium skin tone
People & Body	🤴🏾	prince: medium-dark skin tone
People & Body	🤴🏿	prince: dark skin tone
People & Body	👸	princess
People & Body	👸🏻	princess: light skin tone
People & Body	👸🏼	princess: medium-light skin tone
People & Body	👸🏽	princess: medium skin tone
People & Body	👸🏾	princess: medium-dark skin tone
People & Body	👸🏿	princess: dark skin tone
People & Body	👳	person wearing turban
People & Body	👳🏻	person wearing turban: light skin tone
People & Body	👳🏼	person wearing turban: medium-light skin tone
People & Body	👳🏽	person wearing turban: medium skin tone
People & Body	👳🏾	person wearing turban: medium-dark skin tone
People & Body	👳🏿	person wearing turban: dark skin tone
People & Body	👳‍♂️	man wearing turban
People & Body	👳🏻‍♂️	man wearing turban: light skin tone
People & Body	👳🏼‍♂️	man wearing turban: medium-light skin tone
People & Body	👳🏽‍♂️	man wearing turban: medium skin tone
People & Body	👳🏾‍♂️	man wearing turban: medium-dark skin tone
People & Body	👳🏿‍♂️	man wearing turban: dark skin tone
People & Body	👳‍♀️	woman wearing turban
People & Body	👳🏻‍♀️	woman wearing turban: light skin tone
People & Body	👳🏼‍♀️	woman wearing turban: medium-light skin tone
People & Body	👳🏽‍♀️	woman wearing turban: medium skin tone
People & Body	👳🏾‍♀️	woman wearing turban: medium-dark skin tone
People & Body	👳🏿‍♀️	woman wearing turban: dark skin tone
People & Body	👲	person with skullcap
People & Body	👲🏻	person with skullcap: light skin tone
People & Body	👲🏼	person with skullcap: medium-light skin tone
People & Body	👲🏽	person with skullcap: medium skin tone
People & Body	👲🏾	person with skullcap: medium-dark skin tone
People & Body	👲🏿	person with skullcap: dark skin tone
People & Body	🧕	woman with headscarf
People & Body	🧕🏻	woman with headscarf: light skin tone
People & Body	🧕🏼	woman with headscarf: medium-light skin tone
People & Body	🧕🏽	woman with headscarf: medium skin tone
People & Body	🧕🏾	woman with headscarf: medium-dark skin tone
People & Body	🧕🏿	woman with headscarf: dark skin tone
People & Body	🤵	person in tuxedo
People & Body	🤵🏻	person in tuxedo: light skin tone
People & Body	🤵🏼	person in tuxedo: medium-light skin tone
People & Body	🤵🏽	person in tuxedo: medium skin tone
People & Body	🤵🏾	person in tuxedo: medium-dark skin tone
People & Body	🤵🏿	person in tuxedo: dark skin tone
People & Body	🤵‍♂️	man in tuxedo
People & Body	🤵🏻‍♂️	man in tuxedo: light skin tone
People & Body	🤵🏼‍♂️	man in tuxedo: medium-light skin tone
People & Body	🤵🏽‍♂️	man in tuxedo: medium skin tone
People & Body	🤵🏾‍♂️	man in tuxedo: medium-dark skin tone
People & Body	🤵🏿‍♂️	man in tuxedo: dark skin tone
People & Body	🤵‍♀️	woman in tuxedo
People & Body	🤵🏻‍♀️	woman in tuxedo: light skin tone
People & Body	🤵🏼‍♀️	woman in tuxedo: medium-light skin tone
People & Body	🤵🏽‍♀️	woman in tuxedo: medium skin tone
People & Body	🤵🏾‍♀️	woman in tuxedo: medium-dark skin tone
People & Body	🤵🏿‍♀️	woman in tuxedo: dark skin tone
People & Body	👰	person with veil
People & Body	👰🏻	person with veil: light skin tone
People & Body	👰🏼	person with veil: medium-light skin tone
People & Body	👰🏽	person with veil: medium skin tone
People & Body	👰🏾	person with veil: medium-dark skin tone
People & Body	👰🏿	person with veil: dark skin tone
People & Body	👰‍♂️	man with veil
People & Body	👰🏻‍♂️	man with veil: light skin tone
People & Body	👰🏼‍♂️	man with veil: medium-light skin tone
People & Body	👰🏽‍♂️	man with veil: medium skin tone
People & Body	👰🏾‍♂️	man with veil: medium-dark skin tone
People & Body	👰🏿‍♂️	man with veil: dark skin tone
People & Body	👰‍♀️	woman with veil
People & Body	👰🏻‍♀️	woman with veil: light skin tone
People & Body	👰🏼‍♀️	woman with veil: medium-light skin tone
People & Body	👰🏽‍♀️	woman with veil: medium skin tone
People & Body	👰🏾‍♀️	woman with veil: medium-dark skin tone
People & Body	👰🏿‍♀️	woman with veil: dark skin tone
People & Body	🤰	pregnant woman
People & Body	🤰🏻	pregnant woman: light skin tone
        """,
        """
People & Body	🤰🏼	pregnant woman: medium-light skin tone
People & Body	🤰🏽	pregnant woman: medium skin tone
People & Body	🤰🏾	pregnant woman: medium-dark skin tone
People & Body	🤰🏿	pregnant woman: dark skin tone
People & Body	🫃	pregnant man
People & Body	🫃🏻	pregnant man: light skin tone
People & Body	🫃🏼	pregnant man: medium-light skin tone
People & Body	🫃🏽	pregnant man: medium skin tone
People & Body	🫃🏾	pregnant man: medium-dark skin tone
People & Body	🫃🏿	pregnant man: dark skin tone
People & Body	🫄	pregnant person
People & Body	🫄🏻	pregnant person: light skin tone
People & Body	🫄🏼	pregnant person: medium-light skin tone
People & Body	🫄🏽	pregnant person: medium skin tone
People & Body	🫄🏾	pregnant person: medium-dark skin tone
People & Body	🫄🏿	pregnant person: dark skin tone
People & Body	🤱	breast-feeding
People & Body	🤱🏻	breast-feeding: light skin tone
People & Body	🤱🏼	breast-feeding: medium-light skin tone
People & Body	🤱🏽	breast-feeding: medium skin tone
People & Body	🤱🏾	breast-feeding: medium-dark skin tone
People & Body	🤱🏿	breast-feeding: dark skin tone
People & Body	👩‍🍼	woman feeding baby
People & Body	👩🏻‍🍼	woman feeding baby: light skin tone
People & Body	👩🏼‍🍼	woman feeding baby: medium-light skin tone
People & Body	👩🏽‍🍼	woman feeding baby: medium skin tone
People & Body	👩🏾‍🍼	woman feeding baby: medium-dark skin tone
People & Body	👩🏿‍🍼	woman feeding baby: dark skin tone
People & Body	👨‍🍼	man feeding baby
People & Body	👨🏻‍🍼	man feeding baby: light skin tone
People & Body	👨🏼‍🍼	man feeding baby: medium-light skin tone
People & Body	👨🏽‍🍼	man feeding baby: medium skin tone
People & Body	👨🏾‍🍼	man feeding baby: medium-dark skin tone
People & Body	👨🏿‍🍼	man feeding baby: dark skin tone
People & Body	🧑‍🍼	person feeding baby
People & Body	🧑🏻‍🍼	person feeding baby: light skin tone
People & Body	🧑🏼‍🍼	person feeding baby: medium-light skin tone
People & Body	🧑🏽‍🍼	person feeding baby: medium skin tone
People & Body	🧑🏾‍🍼	person feeding baby: medium-dark skin tone
People & Body	🧑🏿‍🍼	person feeding baby: dark skin tone
People & Body	👼	baby angel
People & Body	👼🏻	baby angel: light skin tone
People & Body	👼🏼	baby angel: medium-light skin tone
People & Body	👼🏽	baby angel: medium skin tone
People & Body	👼🏾	baby angel: medium-dark skin tone
People & Body	👼🏿	baby angel: dark skin tone
People & Body	🎅	Santa Claus
People & Body	🎅🏻	Santa Claus: light skin tone
People & Body	🎅🏼	Santa Claus: medium-light skin tone
People & Body	🎅🏽	Santa Claus: medium skin tone
People & Body	🎅🏾	Santa Claus: medium-dark skin tone
People & Body	🎅🏿	Santa Claus: dark skin tone
People & Body	🤶	Mrs. Claus
People & Body	🤶🏻	Mrs. Claus: light skin tone
People & Body	🤶🏼	Mrs. Claus: medium-light skin tone
People & Body	🤶🏽	Mrs. Claus: medium skin tone
People & Body	🤶🏾	Mrs. Claus: medium-dark skin tone
People & Body	🤶🏿	Mrs. Claus: dark skin tone
People & Body	🧑‍🎄	Mx Claus
People & Body	🧑🏻‍🎄	Mx Claus: light skin tone
People & Body	🧑🏼‍🎄	Mx Claus: medium-light skin tone
People & Body	🧑🏽‍🎄	Mx Claus: medium skin tone
People & Body	🧑🏾‍🎄	Mx Claus: medium-dark skin tone
People & Body	🧑🏿‍🎄	Mx Claus: dark skin tone
People & Body	🦸	superhero
People & Body	🦸🏻	superhero: light skin tone
People & Body	🦸🏼	superhero: medium-light skin tone
People & Body	🦸🏽	superhero: medium skin tone
People & Body	🦸🏾	superhero: medium-dark skin tone
People & Body	🦸🏿	superhero: dark skin tone
People & Body	🦸‍♂️	man superhero
People & Body	🦸🏻‍♂️	man superhero: light skin tone
People & Body	🦸🏼‍♂️	man superhero: medium-light skin tone
People & Body	🦸🏽‍♂️	man superhero: medium skin tone
People & Body	🦸🏾‍♂️	man superhero: medium-dark skin tone
People & Body	🦸🏿‍♂️	man superhero: dark skin tone
People & Body	🦸‍♀️	woman superhero
People & Body	🦸🏻‍♀️	woman superhero: light skin tone
People & Body	🦸🏼‍♀️	woman superhero: medium-light skin tone
People & Body	🦸🏽‍♀️	woman superhero: medium skin tone
People & Body	🦸🏾‍♀️	woman superhero: medium-dark skin tone
People & Body	🦸🏿‍♀️	woman superhero: dark skin tone
People & Body	🦹	supervillain
People & Body	🦹🏻	supervillain: light skin tone
People & Body	🦹🏼	supervillain: medium-light skin tone
People & Body	🦹🏽	supervillain: medium skin tone
People & Body	🦹🏾	supervillain: medium-dark skin tone
People & Body	🦹🏿	supervillain: dark skin tone
People & Body	🦹‍♂️	man supervillain
People & Body	🦹🏻‍♂️	man supervillain: light skin tone
People & Body	🦹🏼‍♂️	man supervillain: medium-light skin tone
People & Body	🦹🏽‍♂️	man supervillain: medium skin tone
People & Body	🦹🏾‍♂️	man supervillain: medium-dark skin tone
People & Body	🦹🏿‍♂️	man supervillain: dark skin tone
People & Body	🦹‍♀️	woman supervillain
People & Body	🦹🏻‍♀️	woman supervillain: light skin tone
People & Body	🦹🏼‍♀️	woman supervillain: medium-light skin tone
People & Body	🦹🏽‍♀️	woman supervillain: medium skin tone
People & Body	🦹🏾‍♀️	woman supervillain: medium-dark skin tone
People & Body	🦹🏿‍♀️	woman supervillain: dark skin tone
People & Body	🧙	mage
People & Body	🧙🏻	mage: light skin tone
People & Body	🧙🏼	mage: medium-light skin tone
People & Body	🧙🏽	mage: medium skin tone
People & Body	🧙🏾	mage: medium-dark skin tone
People & Body	🧙🏿	mage: dark skin tone
People & Body	🧙‍♂️	man mage
People & Body	🧙🏻‍♂️	man mage: light skin tone
People & Body	🧙🏼‍♂️	man mage: medium-light skin tone
People & Body	🧙🏽‍♂️	man mage: medium skin tone
People & Body	🧙🏾‍♂️	man mage: medium-dark skin tone
People & Body	🧙🏿‍♂️	man mage: dark skin tone
People & Body	🧙‍♀️	woman mage
People & Body	🧙🏻‍♀️	woman mage: light skin tone
People & Body	🧙🏼‍♀️	woman mage: medium-light skin tone
People & Body	🧙🏽‍♀️	woman mage: medium skin tone
People & Body	🧙🏾‍♀️	woman mage: medium-dark skin tone
People & Body	🧙🏿‍♀️	woman mage: dark skin tone
People & Body	🧚	fairy
People & Body	🧚🏻	fairy: light skin tone
People & Body	🧚🏼	fairy: medium-light skin tone
People & Body	🧚🏽	fairy: medium skin tone
People & Body	🧚🏾	fairy: medium-dark skin tone
People & Body	🧚🏿	fairy: dark skin tone
People & Body	🧚‍♂️	man fairy
People & Body	🧚🏻‍♂️	man fairy: light skin tone
People & Body	🧚🏼‍♂️	man fairy: medium-light skin tone
People & Body	🧚🏽‍♂️	man fairy: medium skin tone
People & Body	🧚🏾‍♂️	man fairy: medium-dark skin tone
People & Body	🧚🏿‍♂️	man fairy: dark skin tone
People & Body	🧚‍♀️	woman fairy
People & Body	🧚🏻‍♀️	woman fairy: light skin tone
People & Body	🧚🏼‍♀️	woman fairy: medium-light skin tone
People & Body	🧚🏽‍♀️	woman fairy: medium skin tone
People & Body	🧚🏾‍♀️	woman fairy: medium-dark skin tone
People & Body	🧚🏿‍♀️	woman fairy: dark skin tone
People & Body	🧛	vampire
People & Body	🧛🏻	vampire: light skin tone
People & Body	🧛🏼	vampire: medium-light skin tone
People & Body	🧛🏽	vampire: medium skin tone
People & Body	🧛🏾	vampire: medium-dark skin tone
People & Body	🧛🏿	vampire: dark skin tone
People & Body	🧛‍♂️	man vampire
People & Body	🧛🏻‍♂️	man vampire: light skin tone
People & Body	🧛🏼‍♂️	man vampire: medium-light skin tone
People & Body	🧛🏽‍♂️	man vampire: medium skin tone
People & Body	🧛🏾‍♂️	man vampire: medium-dark skin tone
People & Body	🧛🏿‍♂️	man vampire: dark skin tone
People & Body	🧛‍♀️	woman vampire
People & Body	🧛🏻‍♀️	woman vampire: light skin tone
People & Body	🧛🏼‍♀️	woman vampire: medium-light skin tone
People & Body	🧛🏽‍♀️	woman vampire: medium skin tone
People & Body	🧛🏾‍♀️	woman vampire: medium-dark skin tone
People & Body	🧛🏿‍♀️	woman vampire: dark skin tone
People & Body	🧜	merperson
People & Body	🧜🏻	merperson: light skin tone
People & Body	🧜🏼	merperson: medium-light skin tone
People & Body	🧜🏽	merperson: medium skin tone
People & Body	🧜🏾	merperson: medium-dark skin tone
People & Body	🧜🏿	merperson: dark skin tone
People & Body	🧜‍♂️	merman
People & Body	🧜🏻‍♂️	merman: light skin tone
People & Body	🧜🏼‍♂️	merman: medium-light skin tone
People & Body	🧜🏽‍♂️	merman: medium skin tone
People & Body	🧜🏾‍♂️	merman: medium-dark skin tone
People & Body	🧜🏿‍♂️	merman: dark skin tone
People & Body	🧜‍♀️	mermaid
People & Body	🧜🏻‍♀️	mermaid: light skin tone
People & Body	🧜🏼‍♀️	mermaid: medium-light skin tone
People & Body	🧜🏽‍♀️	mermaid: medium skin tone
People & Body	🧜🏾‍♀️	mermaid: medium-dark skin tone
People & Body	🧜🏿‍♀️	mermaid: dark skin tone
People & Body	🧝	elf
People & Body	🧝🏻	elf: light skin tone
People & Body	🧝🏼	elf: medium-light skin tone
People & Body	🧝🏽	elf: medium skin tone
People & Body	🧝🏾	elf: medium-dark skin tone
People & Body	🧝🏿	elf: dark skin tone
People & Body	🧝‍♂️	man elf
People & Body	🧝🏻‍♂️	man elf: light skin tone
People & Body	🧝🏼‍♂️	man elf: medium-light skin tone
People & Body	🧝🏽‍♂️	man elf: medium skin tone
People & Body	🧝🏾‍♂️	man elf: medium-dark skin tone
People & Body	🧝🏿‍♂️	man elf: dark skin tone
People & Body	🧝‍♀️	woman elf
People & Body	🧝🏻‍♀️	woman elf: light skin tone
People & Body	🧝🏼‍♀️	woman elf: medium-light skin tone
People & Body	🧝🏽‍♀️	woman elf: medium skin tone
People & Body	🧝🏾‍♀️	woman elf: medium-dark skin tone
People & Body	🧝🏿‍♀️	woman elf: dark skin tone
People & Body	🧞	genie
People & Body	🧞‍♂️	man genie
People & Body	🧞‍♀️	woman genie
People & Body	🧟	zombie
People & Body	🧟‍♂️	man zombie
People & Body	🧟‍♀️	woman zombie
People & Body	🧌	troll
People & Body	🫈	hairy creature
People & Body	💆	person getting massage
People & Body	💆🏻	person getting massage: light skin tone
People & Body	💆🏼	person getting massage: medium-light skin tone
People & Body	💆🏽	person getting massage: medium skin tone
People & Body	💆🏾	person getting massage: medium-dark skin tone
People & Body	💆🏿	person getting massage: dark skin tone
People & Body	💆‍♂️	man getting massage
People & Body	💆🏻‍♂️	man getting massage: light skin tone
People & Body	💆🏼‍♂️	man getting massage: medium-light skin tone
People & Body	💆🏽‍♂️	man getting massage: medium skin tone
People & Body	💆🏾‍♂️	man getting massage: medium-dark skin tone
People & Body	💆🏿‍♂️	man getting massage: dark skin tone
People & Body	💆‍♀️	woman getting massage
People & Body	💆🏻‍♀️	woman getting massage: light skin tone
People & Body	💆🏼‍♀️	woman getting massage: medium-light skin tone
People & Body	💆🏽‍♀️	woman getting massage: medium skin tone
People & Body	💆🏾‍♀️	woman getting massage: medium-dark skin tone
People & Body	💆🏿‍♀️	woman getting massage: dark skin tone
People & Body	💇	person getting haircut
People & Body	💇🏻	person getting haircut: light skin tone
People & Body	💇🏼	person getting haircut: medium-light skin tone
People & Body	💇🏽	person getting haircut: medium skin tone
People & Body	💇🏾	person getting haircut: medium-dark skin tone
People & Body	💇🏿	person getting haircut: dark skin tone
People & Body	💇‍♂️	man getting haircut
People & Body	💇🏻‍♂️	man getting haircut: light skin tone
People & Body	💇🏼‍♂️	man getting haircut: medium-light skin tone
People & Body	💇🏽‍♂️	man getting haircut: medium skin tone
People & Body	💇🏾‍♂️	man getting haircut: medium-dark skin tone
People & Body	💇🏿‍♂️	man getting haircut: dark skin tone
People & Body	💇‍♀️	woman getting haircut
People & Body	💇🏻‍♀️	woman getting haircut: light skin tone
People & Body	💇🏼‍♀️	woman getting haircut: medium-light skin tone
People & Body	💇🏽‍♀️	woman getting haircut: medium skin tone
People & Body	💇🏾‍♀️	woman getting haircut: medium-dark skin tone
People & Body	💇🏿‍♀️	woman getting haircut: dark skin tone
People & Body	🚶	person walking
People & Body	🚶🏻	person walking: light skin tone
People & Body	🚶🏼	person walking: medium-light skin tone
People & Body	🚶🏽	person walking: medium skin tone
People & Body	🚶🏾	person walking: medium-dark skin tone
People & Body	🚶🏿	person walking: dark skin tone
People & Body	🚶‍♂️	man walking
People & Body	🚶🏻‍♂️	man walking: light skin tone
People & Body	🚶🏼‍♂️	man walking: medium-light skin tone
People & Body	🚶🏽‍♂️	man walking: medium skin tone
People & Body	🚶🏾‍♂️	man walking: medium-dark skin tone
People & Body	🚶🏿‍♂️	man walking: dark skin tone
People & Body	🚶‍♀️	woman walking
People & Body	🚶🏻‍♀️	woman walking: light skin tone
People & Body	🚶🏼‍♀️	woman walking: medium-light skin tone
People & Body	🚶🏽‍♀️	woman walking: medium skin tone
People & Body	🚶🏾‍♀️	woman walking: medium-dark skin tone
People & Body	🚶🏿‍♀️	woman walking: dark skin tone
People & Body	🚶‍➡️	person walking facing right
People & Body	🚶🏻‍➡️	person walking facing right: light skin tone
People & Body	🚶🏼‍➡️	person walking facing right: medium-light skin tone
People & Body	🚶🏽‍➡️	person walking facing right: medium skin tone
People & Body	🚶🏾‍➡️	person walking facing right: medium-dark skin tone
People & Body	🚶🏿‍➡️	person walking facing right: dark skin tone
People & Body	🚶‍♀️‍➡️	woman walking facing right
People & Body	🚶🏻‍♀️‍➡️	woman walking facing right: light skin tone
People & Body	🚶🏼‍♀️‍➡️	woman walking facing right: medium-light skin tone
People & Body	🚶🏽‍♀️‍➡️	woman walking facing right: medium skin tone
People & Body	🚶🏾‍♀️‍➡️	woman walking facing right: medium-dark skin tone
People & Body	🚶🏿‍♀️‍➡️	woman walking facing right: dark skin tone
People & Body	🚶‍♂️‍➡️	man walking facing right
People & Body	🚶🏻‍♂️‍➡️	man walking facing right: light skin tone
People & Body	🚶🏼‍♂️‍➡️	man walking facing right: medium-light skin tone
People & Body	🚶🏽‍♂️‍➡️	man walking facing right: medium skin tone
People & Body	🚶🏾‍♂️‍➡️	man walking facing right: medium-dark skin tone
People & Body	🚶🏿‍♂️‍➡️	man walking facing right: dark skin tone
People & Body	🧍	person standing
People & Body	🧍🏻	person standing: light skin tone
People & Body	🧍🏼	person standing: medium-light skin tone
People & Body	🧍🏽	person standing: medium skin tone
People & Body	🧍🏾	person standing: medium-dark skin tone
People & Body	🧍🏿	person standing: dark skin tone
People & Body	🧍‍♂️	man standing
People & Body	🧍🏻‍♂️	man standing: light skin tone
People & Body	🧍🏼‍♂️	man standing: medium-light skin tone
People & Body	🧍🏽‍♂️	man standing: medium skin tone
People & Body	🧍🏾‍♂️	man standing: medium-dark skin tone
People & Body	🧍🏿‍♂️	man standing: dark skin tone
People & Body	🧍‍♀️	woman standing
People & Body	🧍🏻‍♀️	woman standing: light skin tone
People & Body	🧍🏼‍♀️	woman standing: medium-light skin tone
People & Body	🧍🏽‍♀️	woman standing: medium skin tone
People & Body	🧍🏾‍♀️	woman standing: medium-dark skin tone
People & Body	🧍🏿‍♀️	woman standing: dark skin tone
People & Body	🧎	person kneeling
People & Body	🧎🏻	person kneeling: light skin tone
People & Body	🧎🏼	person kneeling: medium-light skin tone
People & Body	🧎🏽	person kneeling: medium skin tone
People & Body	🧎🏾	person kneeling: medium-dark skin tone
People & Body	🧎🏿	person kneeling: dark skin tone
People & Body	🧎‍♂️	man kneeling
People & Body	🧎🏻‍♂️	man kneeling: light skin tone
People & Body	🧎🏼‍♂️	man kneeling: medium-light skin tone
People & Body	🧎🏽‍♂️	man kneeling: medium skin tone
People & Body	🧎🏾‍♂️	man kneeling: medium-dark skin tone
People & Body	🧎🏿‍♂️	man kneeling: dark skin tone
People & Body	🧎‍♀️	woman kneeling
People & Body	🧎🏻‍♀️	woman kneeling: light skin tone
People & Body	🧎🏼‍♀️	woman kneeling: medium-light skin tone
People & Body	🧎🏽‍♀️	woman kneeling: medium skin tone
People & Body	🧎🏾‍♀️	woman kneeling: medium-dark skin tone
People & Body	🧎🏿‍♀️	woman kneeling: dark skin tone
People & Body	🧎‍➡️	person kneeling facing right
People & Body	🧎🏻‍➡️	person kneeling facing right: light skin tone
        """,
        """
People & Body	🧎🏼‍➡️	person kneeling facing right: medium-light skin tone
People & Body	🧎🏽‍➡️	person kneeling facing right: medium skin tone
People & Body	🧎🏾‍➡️	person kneeling facing right: medium-dark skin tone
People & Body	🧎🏿‍➡️	person kneeling facing right: dark skin tone
People & Body	🧎‍♀️‍➡️	woman kneeling facing right
People & Body	🧎🏻‍♀️‍➡️	woman kneeling facing right: light skin tone
People & Body	🧎🏼‍♀️‍➡️	woman kneeling facing right: medium-light skin tone
People & Body	🧎🏽‍♀️‍➡️	woman kneeling facing right: medium skin tone
People & Body	🧎🏾‍♀️‍➡️	woman kneeling facing right: medium-dark skin tone
People & Body	🧎🏿‍♀️‍➡️	woman kneeling facing right: dark skin tone
People & Body	🧎‍♂️‍➡️	man kneeling facing right
People & Body	🧎🏻‍♂️‍➡️	man kneeling facing right: light skin tone
People & Body	🧎🏼‍♂️‍➡️	man kneeling facing right: medium-light skin tone
People & Body	🧎🏽‍♂️‍➡️	man kneeling facing right: medium skin tone
People & Body	🧎🏾‍♂️‍➡️	man kneeling facing right: medium-dark skin tone
People & Body	🧎🏿‍♂️‍➡️	man kneeling facing right: dark skin tone
People & Body	🧑‍🦯	person with white cane
People & Body	🧑🏻‍🦯	person with white cane: light skin tone
People & Body	🧑🏼‍🦯	person with white cane: medium-light skin tone
People & Body	🧑🏽‍🦯	person with white cane: medium skin tone
People & Body	🧑🏾‍🦯	person with white cane: medium-dark skin tone
People & Body	🧑🏿‍🦯	person with white cane: dark skin tone
People & Body	🧑‍🦯‍➡️	person with white cane facing right
People & Body	🧑🏻‍🦯‍➡️	person with white cane facing right: light skin tone
People & Body	🧑🏼‍🦯‍➡️	person with white cane facing right: medium-light skin tone
People & Body	🧑🏽‍🦯‍➡️	person with white cane facing right: medium skin tone
People & Body	🧑🏾‍🦯‍➡️	person with white cane facing right: medium-dark skin tone
People & Body	🧑🏿‍🦯‍➡️	person with white cane facing right: dark skin tone
People & Body	👨‍🦯	man with white cane
People & Body	👨🏻‍🦯	man with white cane: light skin tone
People & Body	👨🏼‍🦯	man with white cane: medium-light skin tone
People & Body	👨🏽‍🦯	man with white cane: medium skin tone
People & Body	👨🏾‍🦯	man with white cane: medium-dark skin tone
People & Body	👨🏿‍🦯	man with white cane: dark skin tone
People & Body	👨‍🦯‍➡️	man with white cane facing right
People & Body	👨🏻‍🦯‍➡️	man with white cane facing right: light skin tone
People & Body	👨🏼‍🦯‍➡️	man with white cane facing right: medium-light skin tone
People & Body	👨🏽‍🦯‍➡️	man with white cane facing right: medium skin tone
People & Body	👨🏾‍🦯‍➡️	man with white cane facing right: medium-dark skin tone
People & Body	👨🏿‍🦯‍➡️	man with white cane facing right: dark skin tone
People & Body	👩‍🦯	woman with white cane
People & Body	👩🏻‍🦯	woman with white cane: light skin tone
People & Body	👩🏼‍🦯	woman with white cane: medium-light skin tone
People & Body	👩🏽‍🦯	woman with white cane: medium skin tone
People & Body	👩🏾‍🦯	woman with white cane: medium-dark skin tone
People & Body	👩🏿‍🦯	woman with white cane: dark skin tone
People & Body	👩‍🦯‍➡️	woman with white cane facing right
People & Body	👩🏻‍🦯‍➡️	woman with white cane facing right: light skin tone
People & Body	👩🏼‍🦯‍➡️	woman with white cane facing right: medium-light skin tone
People & Body	👩🏽‍🦯‍➡️	woman with white cane facing right: medium skin tone
People & Body	👩🏾‍🦯‍➡️	woman with white cane facing right: medium-dark skin tone
People & Body	👩🏿‍🦯‍➡️	woman with white cane facing right: dark skin tone
People & Body	🧑‍🦼	person in motorized wheelchair
People & Body	🧑🏻‍🦼	person in motorized wheelchair: light skin tone
People & Body	🧑🏼‍🦼	person in motorized wheelchair: medium-light skin tone
People & Body	🧑🏽‍🦼	person in motorized wheelchair: medium skin tone
People & Body	🧑🏾‍🦼	person in motorized wheelchair: medium-dark skin tone
People & Body	🧑🏿‍🦼	person in motorized wheelchair: dark skin tone
People & Body	🧑‍🦼‍➡️	person in motorized wheelchair facing right
People & Body	🧑🏻‍🦼‍➡️	person in motorized wheelchair facing right: light skin tone
People & Body	🧑🏼‍🦼‍➡️	person in motorized wheelchair facing right: medium-light skin tone
People & Body	🧑🏽‍🦼‍➡️	person in motorized wheelchair facing right: medium skin tone
People & Body	🧑🏾‍🦼‍➡️	person in motorized wheelchair facing right: medium-dark skin tone
People & Body	🧑🏿‍🦼‍➡️	person in motorized wheelchair facing right: dark skin tone
People & Body	👨‍🦼	man in motorized wheelchair
People & Body	👨🏻‍🦼	man in motorized wheelchair: light skin tone
People & Body	👨🏼‍🦼	man in motorized wheelchair: medium-light skin tone
People & Body	👨🏽‍🦼	man in motorized wheelchair: medium skin tone
People & Body	👨🏾‍🦼	man in motorized wheelchair: medium-dark skin tone
People & Body	👨🏿‍🦼	man in motorized wheelchair: dark skin tone
People & Body	👨‍🦼‍➡️	man in motorized wheelchair facing right
People & Body	👨🏻‍🦼‍➡️	man in motorized wheelchair facing right: light skin tone
People & Body	👨🏼‍🦼‍➡️	man in motorized wheelchair facing right: medium-light skin tone
People & Body	👨🏽‍🦼‍➡️	man in motorized wheelchair facing right: medium skin tone
People & Body	👨🏾‍🦼‍➡️	man in motorized wheelchair facing right: medium-dark skin tone
People & Body	👨🏿‍🦼‍➡️	man in motorized wheelchair facing right: dark skin tone
People & Body	👩‍🦼	woman in motorized wheelchair
People & Body	👩🏻‍🦼	woman in motorized wheelchair: light skin tone
People & Body	👩🏼‍🦼	woman in motorized wheelchair: medium-light skin tone
People & Body	👩🏽‍🦼	woman in motorized wheelchair: medium skin tone
People & Body	👩🏾‍🦼	woman in motorized wheelchair: medium-dark skin tone
People & Body	👩🏿‍🦼	woman in motorized wheelchair: dark skin tone
People & Body	👩‍🦼‍➡️	woman in motorized wheelchair facing right
People & Body	👩🏻‍🦼‍➡️	woman in motorized wheelchair facing right: light skin tone
People & Body	👩🏼‍🦼‍➡️	woman in motorized wheelchair facing right: medium-light skin tone
People & Body	👩🏽‍🦼‍➡️	woman in motorized wheelchair facing right: medium skin tone
People & Body	👩🏾‍🦼‍➡️	woman in motorized wheelchair facing right: medium-dark skin tone
People & Body	👩🏿‍🦼‍➡️	woman in motorized wheelchair facing right: dark skin tone
People & Body	🧑‍🦽	person in manual wheelchair
People & Body	🧑🏻‍🦽	person in manual wheelchair: light skin tone
People & Body	🧑🏼‍🦽	person in manual wheelchair: medium-light skin tone
People & Body	🧑🏽‍🦽	person in manual wheelchair: medium skin tone
People & Body	🧑🏾‍🦽	person in manual wheelchair: medium-dark skin tone
People & Body	🧑🏿‍🦽	person in manual wheelchair: dark skin tone
People & Body	🧑‍🦽‍➡️	person in manual wheelchair facing right
People & Body	🧑🏻‍🦽‍➡️	person in manual wheelchair facing right: light skin tone
People & Body	🧑🏼‍🦽‍➡️	person in manual wheelchair facing right: medium-light skin tone
People & Body	🧑🏽‍🦽‍➡️	person in manual wheelchair facing right: medium skin tone
People & Body	🧑🏾‍🦽‍➡️	person in manual wheelchair facing right: medium-dark skin tone
People & Body	🧑🏿‍🦽‍➡️	person in manual wheelchair facing right: dark skin tone
People & Body	👨‍🦽	man in manual wheelchair
People & Body	👨🏻‍🦽	man in manual wheelchair: light skin tone
People & Body	👨🏼‍🦽	man in manual wheelchair: medium-light skin tone
People & Body	👨🏽‍🦽	man in manual wheelchair: medium skin tone
People & Body	👨🏾‍🦽	man in manual wheelchair: medium-dark skin tone
People & Body	👨🏿‍🦽	man in manual wheelchair: dark skin tone
People & Body	👨‍🦽‍➡️	man in manual wheelchair facing right
People & Body	👨🏻‍🦽‍➡️	man in manual wheelchair facing right: light skin tone
People & Body	👨🏼‍🦽‍➡️	man in manual wheelchair facing right: medium-light skin tone
People & Body	👨🏽‍🦽‍➡️	man in manual wheelchair facing right: medium skin tone
People & Body	👨🏾‍🦽‍➡️	man in manual wheelchair facing right: medium-dark skin tone
People & Body	👨🏿‍🦽‍➡️	man in manual wheelchair facing right: dark skin tone
People & Body	👩‍🦽	woman in manual wheelchair
People & Body	👩🏻‍🦽	woman in manual wheelchair: light skin tone
People & Body	👩🏼‍🦽	woman in manual wheelchair: medium-light skin tone
People & Body	👩🏽‍🦽	woman in manual wheelchair: medium skin tone
People & Body	👩🏾‍🦽	woman in manual wheelchair: medium-dark skin tone
People & Body	👩🏿‍🦽	woman in manual wheelchair: dark skin tone
People & Body	👩‍🦽‍➡️	woman in manual wheelchair facing right
People & Body	👩🏻‍🦽‍➡️	woman in manual wheelchair facing right: light skin tone
People & Body	👩🏼‍🦽‍➡️	woman in manual wheelchair facing right: medium-light skin tone
People & Body	👩🏽‍🦽‍➡️	woman in manual wheelchair facing right: medium skin tone
People & Body	👩🏾‍🦽‍➡️	woman in manual wheelchair facing right: medium-dark skin tone
People & Body	👩🏿‍🦽‍➡️	woman in manual wheelchair facing right: dark skin tone
People & Body	🏃	person running
People & Body	🏃🏻	person running: light skin tone
People & Body	🏃🏼	person running: medium-light skin tone
People & Body	🏃🏽	person running: medium skin tone
People & Body	🏃🏾	person running: medium-dark skin tone
People & Body	🏃🏿	person running: dark skin tone
People & Body	🏃‍♂️	man running
People & Body	🏃🏻‍♂️	man running: light skin tone
People & Body	🏃🏼‍♂️	man running: medium-light skin tone
People & Body	🏃🏽‍♂️	man running: medium skin tone
People & Body	🏃🏾‍♂️	man running: medium-dark skin tone
People & Body	🏃🏿‍♂️	man running: dark skin tone
People & Body	🏃‍♀️	woman running
People & Body	🏃🏻‍♀️	woman running: light skin tone
People & Body	🏃🏼‍♀️	woman running: medium-light skin tone
People & Body	🏃🏽‍♀️	woman running: medium skin tone
People & Body	🏃🏾‍♀️	woman running: medium-dark skin tone
People & Body	🏃🏿‍♀️	woman running: dark skin tone
People & Body	🏃‍➡️	person running facing right
People & Body	🏃🏻‍➡️	person running facing right: light skin tone
People & Body	🏃🏼‍➡️	person running facing right: medium-light skin tone
People & Body	🏃🏽‍➡️	person running facing right: medium skin tone
People & Body	🏃🏾‍➡️	person running facing right: medium-dark skin tone
People & Body	🏃🏿‍➡️	person running facing right: dark skin tone
People & Body	🏃‍♀️‍➡️	woman running facing right
People & Body	🏃🏻‍♀️‍➡️	woman running facing right: light skin tone
People & Body	🏃🏼‍♀️‍➡️	woman running facing right: medium-light skin tone
People & Body	🏃🏽‍♀️‍➡️	woman running facing right: medium skin tone
People & Body	🏃🏾‍♀️‍➡️	woman running facing right: medium-dark skin tone
People & Body	🏃🏿‍♀️‍➡️	woman running facing right: dark skin tone
People & Body	🏃‍♂️‍➡️	man running facing right
People & Body	🏃🏻‍♂️‍➡️	man running facing right: light skin tone
People & Body	🏃🏼‍♂️‍➡️	man running facing right: medium-light skin tone
People & Body	🏃🏽‍♂️‍➡️	man running facing right: medium skin tone
People & Body	🏃🏾‍♂️‍➡️	man running facing right: medium-dark skin tone
People & Body	🏃🏿‍♂️‍➡️	man running facing right: dark skin tone
People & Body	🧑‍🩰	ballet dancer
People & Body	🧑🏻‍🩰	ballet dancer: light skin tone
People & Body	🧑🏼‍🩰	ballet dancer: medium-light skin tone
People & Body	🧑🏽‍🩰	ballet dancer: medium skin tone
People & Body	🧑🏾‍🩰	ballet dancer: medium-dark skin tone
People & Body	🧑🏿‍🩰	ballet dancer: dark skin tone
People & Body	💃	woman dancing
People & Body	💃🏻	woman dancing: light skin tone
People & Body	💃🏼	woman dancing: medium-light skin tone
People & Body	💃🏽	woman dancing: medium skin tone
People & Body	💃🏾	woman dancing: medium-dark skin tone
People & Body	💃🏿	woman dancing: dark skin tone
People & Body	🕺	man dancing
People & Body	🕺🏻	man dancing: light skin tone
People & Body	🕺🏼	man dancing: medium-light skin tone
People & Body	🕺🏽	man dancing: medium skin tone
People & Body	🕺🏾	man dancing: medium-dark skin tone
People & Body	🕺🏿	man dancing: dark skin tone
People & Body	🕴️	person in suit levitating
People & Body	🕴🏻	person in suit levitating: light skin tone
People & Body	🕴🏼	person in suit levitating: medium-light skin tone
People & Body	🕴🏽	person in suit levitating: medium skin tone
People & Body	🕴🏾	person in suit levitating: medium-dark skin tone
People & Body	🕴🏿	person in suit levitating: dark skin tone
People & Body	👯	people with bunny ears
People & Body	👯🏻	people with bunny ears: light skin tone
People & Body	👯🏼	people with bunny ears: medium-light skin tone
People & Body	👯🏽	people with bunny ears: medium skin tone
People & Body	👯🏾	people with bunny ears: medium-dark skin tone
People & Body	👯🏿	people with bunny ears: dark skin tone
People & Body	👯‍♂️	men with bunny ears
People & Body	👯🏻‍♂️	men with bunny ears: light skin tone
People & Body	👯🏼‍♂️	men with bunny ears: medium-light skin tone
People & Body	👯🏽‍♂️	men with bunny ears: medium skin tone
People & Body	👯🏾‍♂️	men with bunny ears: medium-dark skin tone
People & Body	👯🏿‍♂️	men with bunny ears: dark skin tone
People & Body	👯‍♀️	women with bunny ears
People & Body	👯🏻‍♀️	women with bunny ears: light skin tone
People & Body	👯🏼‍♀️	women with bunny ears: medium-light skin tone
People & Body	👯🏽‍♀️	women with bunny ears: medium skin tone
People & Body	👯🏾‍♀️	women with bunny ears: medium-dark skin tone
People & Body	👯🏿‍♀️	women with bunny ears: dark skin tone
People & Body	🧑🏻‍🐰‍🧑🏼	people with bunny ears: light skin tone, medium-light skin tone
People & Body	🧑🏻‍🐰‍🧑🏽	people with bunny ears: light skin tone, medium skin tone
People & Body	🧑🏻‍🐰‍🧑🏾	people with bunny ears: light skin tone, medium-dark skin tone
People & Body	🧑🏻‍🐰‍🧑🏿	people with bunny ears: light skin tone, dark skin tone
People & Body	🧑🏼‍🐰‍🧑🏻	people with bunny ears: medium-light skin tone, light skin tone
People & Body	🧑🏼‍🐰‍🧑🏽	people with bunny ears: medium-light skin tone, medium skin tone
People & Body	🧑🏼‍🐰‍🧑🏾	people with bunny ears: medium-light skin tone, medium-dark skin tone
People & Body	🧑🏼‍🐰‍🧑🏿	people with bunny ears: medium-light skin tone, dark skin tone
People & Body	🧑🏽‍🐰‍🧑🏻	people with bunny ears: medium skin tone, light skin tone
People & Body	🧑🏽‍🐰‍🧑🏼	people with bunny ears: medium skin tone, medium-light skin tone
People & Body	🧑🏽‍🐰‍🧑🏾	people with bunny ears: medium skin tone, medium-dark skin tone
People & Body	🧑🏽‍🐰‍🧑🏿	people with bunny ears: medium skin tone, dark skin tone
People & Body	🧑🏾‍🐰‍🧑🏻	people with bunny ears: medium-dark skin tone, light skin tone
People & Body	🧑🏾‍🐰‍🧑🏼	people with bunny ears: medium-dark skin tone, medium-light skin tone
People & Body	🧑🏾‍🐰‍🧑🏽	people with bunny ears: medium-dark skin tone, medium skin tone
People & Body	🧑🏾‍🐰‍🧑🏿	people with bunny ears: medium-dark skin tone, dark skin tone
People & Body	🧑🏿‍🐰‍🧑🏻	people with bunny ears: dark skin tone, light skin tone
People & Body	🧑🏿‍🐰‍🧑🏼	people with bunny ears: dark skin tone, medium-light skin tone
People & Body	🧑🏿‍🐰‍🧑🏽	people with bunny ears: dark skin tone, medium skin tone
People & Body	🧑🏿‍🐰‍🧑🏾	people with bunny ears: dark skin tone, medium-dark skin tone
People & Body	👨🏻‍🐰‍👨🏼	men with bunny ears: light skin tone, medium-light skin tone
People & Body	👨🏻‍🐰‍👨🏽	men with bunny ears: light skin tone, medium skin tone
People & Body	👨🏻‍🐰‍👨🏾	men with bunny ears: light skin tone, medium-dark skin tone
People & Body	👨🏻‍🐰‍👨🏿	men with bunny ears: light skin tone, dark skin tone
People & Body	👨🏼‍🐰‍👨🏻	men with bunny ears: medium-light skin tone, light skin tone
People & Body	👨🏼‍🐰‍👨🏽	men with bunny ears: medium-light skin tone, medium skin tone
        """,
        """
People & Body	👨🏼‍🐰‍👨🏾	men with bunny ears: medium-light skin tone, medium-dark skin tone
People & Body	👨🏼‍🐰‍👨🏿	men with bunny ears: medium-light skin tone, dark skin tone
People & Body	👨🏽‍🐰‍👨🏻	men with bunny ears: medium skin tone, light skin tone
People & Body	👨🏽‍🐰‍👨🏼	men with bunny ears: medium skin tone, medium-light skin tone
People & Body	👨🏽‍🐰‍👨🏾	men with bunny ears: medium skin tone, medium-dark skin tone
People & Body	👨🏽‍🐰‍👨🏿	men with bunny ears: medium skin tone, dark skin tone
People & Body	👨🏾‍🐰‍👨🏻	men with bunny ears: medium-dark skin tone, light skin tone
People & Body	👨🏾‍🐰‍👨🏼	men with bunny ears: medium-dark skin tone, medium-light skin tone
People & Body	👨🏾‍🐰‍👨🏽	men with bunny ears: medium-dark skin tone, medium skin tone
People & Body	👨🏾‍🐰‍👨🏿	men with bunny ears: medium-dark skin tone, dark skin tone
People & Body	👨🏿‍🐰‍👨🏻	men with bunny ears: dark skin tone, light skin tone
People & Body	👨🏿‍🐰‍👨🏼	men with bunny ears: dark skin tone, medium-light skin tone
People & Body	👨🏿‍🐰‍👨🏽	men with bunny ears: dark skin tone, medium skin tone
People & Body	👨🏿‍🐰‍👨🏾	men with bunny ears: dark skin tone, medium-dark skin tone
People & Body	👩🏻‍🐰‍👩🏼	women with bunny ears: light skin tone, medium-light skin tone
People & Body	👩🏻‍🐰‍👩🏽	women with bunny ears: light skin tone, medium skin tone
People & Body	👩🏻‍🐰‍👩🏾	women with bunny ears: light skin tone, medium-dark skin tone
People & Body	👩🏻‍🐰‍👩🏿	women with bunny ears: light skin tone, dark skin tone
People & Body	👩🏼‍🐰‍👩🏻	women with bunny ears: medium-light skin tone, light skin tone
People & Body	👩🏼‍🐰‍👩🏽	women with bunny ears: medium-light skin tone, medium skin tone
People & Body	👩🏼‍🐰‍👩🏾	women with bunny ears: medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍🐰‍👩🏿	women with bunny ears: medium-light skin tone, dark skin tone
People & Body	👩🏽‍🐰‍👩🏻	women with bunny ears: medium skin tone, light skin tone
People & Body	👩🏽‍🐰‍👩🏼	women with bunny ears: medium skin tone, medium-light skin tone
People & Body	👩🏽‍🐰‍👩🏾	women with bunny ears: medium skin tone, medium-dark skin tone
People & Body	👩🏽‍🐰‍👩🏿	women with bunny ears: medium skin tone, dark skin tone
People & Body	👩🏾‍🐰‍👩🏻	women with bunny ears: medium-dark skin tone, light skin tone
People & Body	👩🏾‍🐰‍👩🏼	women with bunny ears: medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍🐰‍👩🏽	women with bunny ears: medium-dark skin tone, medium skin tone
People & Body	👩🏾‍🐰‍👩🏿	women with bunny ears: medium-dark skin tone, dark skin tone
People & Body	👩🏿‍🐰‍👩🏻	women with bunny ears: dark skin tone, light skin tone
People & Body	👩🏿‍🐰‍👩🏼	women with bunny ears: dark skin tone, medium-light skin tone
People & Body	👩🏿‍🐰‍👩🏽	women with bunny ears: dark skin tone, medium skin tone
People & Body	👩🏿‍🐰‍👩🏾	women with bunny ears: dark skin tone, medium-dark skin tone
People & Body	🧖	person in steamy room
People & Body	🧖🏻	person in steamy room: light skin tone
People & Body	🧖🏼	person in steamy room: medium-light skin tone
People & Body	🧖🏽	person in steamy room: medium skin tone
People & Body	🧖🏾	person in steamy room: medium-dark skin tone
People & Body	🧖🏿	person in steamy room: dark skin tone
People & Body	🧖‍♂️	man in steamy room
People & Body	🧖🏻‍♂️	man in steamy room: light skin tone
People & Body	🧖🏼‍♂️	man in steamy room: medium-light skin tone
People & Body	🧖🏽‍♂️	man in steamy room: medium skin tone
People & Body	🧖🏾‍♂️	man in steamy room: medium-dark skin tone
People & Body	🧖🏿‍♂️	man in steamy room: dark skin tone
People & Body	🧖‍♀️	woman in steamy room
People & Body	🧖🏻‍♀️	woman in steamy room: light skin tone
People & Body	🧖🏼‍♀️	woman in steamy room: medium-light skin tone
People & Body	🧖🏽‍♀️	woman in steamy room: medium skin tone
People & Body	🧖🏾‍♀️	woman in steamy room: medium-dark skin tone
People & Body	🧖🏿‍♀️	woman in steamy room: dark skin tone
People & Body	🧗	person climbing
People & Body	🧗🏻	person climbing: light skin tone
People & Body	🧗🏼	person climbing: medium-light skin tone
People & Body	🧗🏽	person climbing: medium skin tone
People & Body	🧗🏾	person climbing: medium-dark skin tone
People & Body	🧗🏿	person climbing: dark skin tone
People & Body	🧗‍♂️	man climbing
People & Body	🧗🏻‍♂️	man climbing: light skin tone
People & Body	🧗🏼‍♂️	man climbing: medium-light skin tone
People & Body	🧗🏽‍♂️	man climbing: medium skin tone
People & Body	🧗🏾‍♂️	man climbing: medium-dark skin tone
People & Body	🧗🏿‍♂️	man climbing: dark skin tone
People & Body	🧗‍♀️	woman climbing
People & Body	🧗🏻‍♀️	woman climbing: light skin tone
People & Body	🧗🏼‍♀️	woman climbing: medium-light skin tone
People & Body	🧗🏽‍♀️	woman climbing: medium skin tone
People & Body	🧗🏾‍♀️	woman climbing: medium-dark skin tone
People & Body	🧗🏿‍♀️	woman climbing: dark skin tone
People & Body	🤺	person fencing
People & Body	🏇	horse racing
People & Body	🏇🏻	horse racing: light skin tone
People & Body	🏇🏼	horse racing: medium-light skin tone
People & Body	🏇🏽	horse racing: medium skin tone
People & Body	🏇🏾	horse racing: medium-dark skin tone
People & Body	🏇🏿	horse racing: dark skin tone
People & Body	⛷️	skier
People & Body	🏂	snowboarder
People & Body	🏂🏻	snowboarder: light skin tone
People & Body	🏂🏼	snowboarder: medium-light skin tone
People & Body	🏂🏽	snowboarder: medium skin tone
People & Body	🏂🏾	snowboarder: medium-dark skin tone
People & Body	🏂🏿	snowboarder: dark skin tone
People & Body	🏌️	person golfing
People & Body	🏌🏻	person golfing: light skin tone
People & Body	🏌🏼	person golfing: medium-light skin tone
People & Body	🏌🏽	person golfing: medium skin tone
People & Body	🏌🏾	person golfing: medium-dark skin tone
People & Body	🏌🏿	person golfing: dark skin tone
People & Body	🏌️‍♂️	man golfing
People & Body	🏌🏻‍♂️	man golfing: light skin tone
People & Body	🏌🏼‍♂️	man golfing: medium-light skin tone
People & Body	🏌🏽‍♂️	man golfing: medium skin tone
People & Body	🏌🏾‍♂️	man golfing: medium-dark skin tone
People & Body	🏌🏿‍♂️	man golfing: dark skin tone
People & Body	🏌️‍♀️	woman golfing
People & Body	🏌🏻‍♀️	woman golfing: light skin tone
People & Body	🏌🏼‍♀️	woman golfing: medium-light skin tone
People & Body	🏌🏽‍♀️	woman golfing: medium skin tone
People & Body	🏌🏾‍♀️	woman golfing: medium-dark skin tone
People & Body	🏌🏿‍♀️	woman golfing: dark skin tone
People & Body	🏄	person surfing
People & Body	🏄🏻	person surfing: light skin tone
People & Body	🏄🏼	person surfing: medium-light skin tone
People & Body	🏄🏽	person surfing: medium skin tone
People & Body	🏄🏾	person surfing: medium-dark skin tone
People & Body	🏄🏿	person surfing: dark skin tone
People & Body	🏄‍♂️	man surfing
People & Body	🏄🏻‍♂️	man surfing: light skin tone
People & Body	🏄🏼‍♂️	man surfing: medium-light skin tone
People & Body	🏄🏽‍♂️	man surfing: medium skin tone
People & Body	🏄🏾‍♂️	man surfing: medium-dark skin tone
People & Body	🏄🏿‍♂️	man surfing: dark skin tone
People & Body	🏄‍♀️	woman surfing
People & Body	🏄🏻‍♀️	woman surfing: light skin tone
People & Body	🏄🏼‍♀️	woman surfing: medium-light skin tone
People & Body	🏄🏽‍♀️	woman surfing: medium skin tone
People & Body	🏄🏾‍♀️	woman surfing: medium-dark skin tone
People & Body	🏄🏿‍♀️	woman surfing: dark skin tone
People & Body	🚣	person rowing boat
People & Body	🚣🏻	person rowing boat: light skin tone
People & Body	🚣🏼	person rowing boat: medium-light skin tone
People & Body	🚣🏽	person rowing boat: medium skin tone
People & Body	🚣🏾	person rowing boat: medium-dark skin tone
People & Body	🚣🏿	person rowing boat: dark skin tone
People & Body	🚣‍♂️	man rowing boat
People & Body	🚣🏻‍♂️	man rowing boat: light skin tone
People & Body	🚣🏼‍♂️	man rowing boat: medium-light skin tone
People & Body	🚣🏽‍♂️	man rowing boat: medium skin tone
People & Body	🚣🏾‍♂️	man rowing boat: medium-dark skin tone
People & Body	🚣🏿‍♂️	man rowing boat: dark skin tone
People & Body	🚣‍♀️	woman rowing boat
People & Body	🚣🏻‍♀️	woman rowing boat: light skin tone
People & Body	🚣🏼‍♀️	woman rowing boat: medium-light skin tone
People & Body	🚣🏽‍♀️	woman rowing boat: medium skin tone
People & Body	🚣🏾‍♀️	woman rowing boat: medium-dark skin tone
People & Body	🚣🏿‍♀️	woman rowing boat: dark skin tone
People & Body	🏊	person swimming
People & Body	🏊🏻	person swimming: light skin tone
People & Body	🏊🏼	person swimming: medium-light skin tone
People & Body	🏊🏽	person swimming: medium skin tone
People & Body	🏊🏾	person swimming: medium-dark skin tone
People & Body	🏊🏿	person swimming: dark skin tone
People & Body	🏊‍♂️	man swimming
People & Body	🏊🏻‍♂️	man swimming: light skin tone
People & Body	🏊🏼‍♂️	man swimming: medium-light skin tone
People & Body	🏊🏽‍♂️	man swimming: medium skin tone
People & Body	🏊🏾‍♂️	man swimming: medium-dark skin tone
People & Body	🏊🏿‍♂️	man swimming: dark skin tone
People & Body	🏊‍♀️	woman swimming
People & Body	🏊🏻‍♀️	woman swimming: light skin tone
People & Body	🏊🏼‍♀️	woman swimming: medium-light skin tone
People & Body	🏊🏽‍♀️	woman swimming: medium skin tone
People & Body	🏊🏾‍♀️	woman swimming: medium-dark skin tone
People & Body	🏊🏿‍♀️	woman swimming: dark skin tone
People & Body	⛹️	person bouncing ball
People & Body	⛹🏻	person bouncing ball: light skin tone
People & Body	⛹🏼	person bouncing ball: medium-light skin tone
People & Body	⛹🏽	person bouncing ball: medium skin tone
People & Body	⛹🏾	person bouncing ball: medium-dark skin tone
People & Body	⛹🏿	person bouncing ball: dark skin tone
People & Body	⛹️‍♂️	man bouncing ball
People & Body	⛹🏻‍♂️	man bouncing ball: light skin tone
People & Body	⛹🏼‍♂️	man bouncing ball: medium-light skin tone
People & Body	⛹🏽‍♂️	man bouncing ball: medium skin tone
People & Body	⛹🏾‍♂️	man bouncing ball: medium-dark skin tone
People & Body	⛹🏿‍♂️	man bouncing ball: dark skin tone
People & Body	⛹️‍♀️	woman bouncing ball
People & Body	⛹🏻‍♀️	woman bouncing ball: light skin tone
People & Body	⛹🏼‍♀️	woman bouncing ball: medium-light skin tone
People & Body	⛹🏽‍♀️	woman bouncing ball: medium skin tone
People & Body	⛹🏾‍♀️	woman bouncing ball: medium-dark skin tone
People & Body	⛹🏿‍♀️	woman bouncing ball: dark skin tone
People & Body	🏋️	person lifting weights
People & Body	🏋🏻	person lifting weights: light skin tone
People & Body	🏋🏼	person lifting weights: medium-light skin tone
People & Body	🏋🏽	person lifting weights: medium skin tone
People & Body	🏋🏾	person lifting weights: medium-dark skin tone
People & Body	🏋🏿	person lifting weights: dark skin tone
People & Body	🏋️‍♂️	man lifting weights
People & Body	🏋🏻‍♂️	man lifting weights: light skin tone
People & Body	🏋🏼‍♂️	man lifting weights: medium-light skin tone
People & Body	🏋🏽‍♂️	man lifting weights: medium skin tone
People & Body	🏋🏾‍♂️	man lifting weights: medium-dark skin tone
People & Body	🏋🏿‍♂️	man lifting weights: dark skin tone
People & Body	🏋️‍♀️	woman lifting weights
People & Body	🏋🏻‍♀️	woman lifting weights: light skin tone
People & Body	🏋🏼‍♀️	woman lifting weights: medium-light skin tone
People & Body	🏋🏽‍♀️	woman lifting weights: medium skin tone
People & Body	🏋🏾‍♀️	woman lifting weights: medium-dark skin tone
People & Body	🏋🏿‍♀️	woman lifting weights: dark skin tone
People & Body	🚴	person biking
People & Body	🚴🏻	person biking: light skin tone
People & Body	🚴🏼	person biking: medium-light skin tone
People & Body	🚴🏽	person biking: medium skin tone
People & Body	🚴🏾	person biking: medium-dark skin tone
People & Body	🚴🏿	person biking: dark skin tone
People & Body	🚴‍♂️	man biking
People & Body	🚴🏻‍♂️	man biking: light skin tone
People & Body	🚴🏼‍♂️	man biking: medium-light skin tone
People & Body	🚴🏽‍♂️	man biking: medium skin tone
People & Body	🚴🏾‍♂️	man biking: medium-dark skin tone
People & Body	🚴🏿‍♂️	man biking: dark skin tone
People & Body	🚴‍♀️	woman biking
People & Body	🚴🏻‍♀️	woman biking: light skin tone
People & Body	🚴🏼‍♀️	woman biking: medium-light skin tone
People & Body	🚴🏽‍♀️	woman biking: medium skin tone
People & Body	🚴🏾‍♀️	woman biking: medium-dark skin tone
People & Body	🚴🏿‍♀️	woman biking: dark skin tone
People & Body	🚵	person mountain biking
People & Body	🚵🏻	person mountain biking: light skin tone
People & Body	🚵🏼	person mountain biking: medium-light skin tone
People & Body	🚵🏽	person mountain biking: medium skin tone
People & Body	🚵🏾	person mountain biking: medium-dark skin tone
People & Body	🚵🏿	person mountain biking: dark skin tone
People & Body	🚵‍♂️	man mountain biking
People & Body	🚵🏻‍♂️	man mountain biking: light skin tone
People & Body	🚵🏼‍♂️	man mountain biking: medium-light skin tone
People & Body	🚵🏽‍♂️	man mountain biking: medium skin tone
People & Body	🚵🏾‍♂️	man mountain biking: medium-dark skin tone
People & Body	🚵🏿‍♂️	man mountain biking: dark skin tone
People & Body	🚵‍♀️	woman mountain biking
People & Body	🚵🏻‍♀️	woman mountain biking: light skin tone
People & Body	🚵🏼‍♀️	woman mountain biking: medium-light skin tone
People & Body	🚵🏽‍♀️	woman mountain biking: medium skin tone
People & Body	🚵🏾‍♀️	woman mountain biking: medium-dark skin tone
People & Body	🚵🏿‍♀️	woman mountain biking: dark skin tone
People & Body	🤸	person cartwheeling
People & Body	🤸🏻	person cartwheeling: light skin tone
People & Body	🤸🏼	person cartwheeling: medium-light skin tone
People & Body	🤸🏽	person cartwheeling: medium skin tone
People & Body	🤸🏾	person cartwheeling: medium-dark skin tone
People & Body	🤸🏿	person cartwheeling: dark skin tone
People & Body	🤸‍♂️	man cartwheeling
People & Body	🤸🏻‍♂️	man cartwheeling: light skin tone
People & Body	🤸🏼‍♂️	man cartwheeling: medium-light skin tone
People & Body	🤸🏽‍♂️	man cartwheeling: medium skin tone
People & Body	🤸🏾‍♂️	man cartwheeling: medium-dark skin tone
People & Body	🤸🏿‍♂️	man cartwheeling: dark skin tone
People & Body	🤸‍♀️	woman cartwheeling
People & Body	🤸🏻‍♀️	woman cartwheeling: light skin tone
People & Body	🤸🏼‍♀️	woman cartwheeling: medium-light skin tone
People & Body	🤸🏽‍♀️	woman cartwheeling: medium skin tone
People & Body	🤸🏾‍♀️	woman cartwheeling: medium-dark skin tone
People & Body	🤸🏿‍♀️	woman cartwheeling: dark skin tone
People & Body	🤼	people wrestling
People & Body	🤼🏻	people wrestling: light skin tone
People & Body	🤼🏼	people wrestling: medium-light skin tone
People & Body	🤼🏽	people wrestling: medium skin tone
People & Body	🤼🏾	people wrestling: medium-dark skin tone
People & Body	🤼🏿	people wrestling: dark skin tone
People & Body	🤼‍♂️	men wrestling
People & Body	🤼🏻‍♂️	men wrestling: light skin tone
People & Body	🤼🏼‍♂️	men wrestling: medium-light skin tone
People & Body	🤼🏽‍♂️	men wrestling: medium skin tone
People & Body	🤼🏾‍♂️	men wrestling: medium-dark skin tone
People & Body	🤼🏿‍♂️	men wrestling: dark skin tone
People & Body	🤼‍♀️	women wrestling
People & Body	🤼🏻‍♀️	women wrestling: light skin tone
People & Body	🤼🏼‍♀️	women wrestling: medium-light skin tone
People & Body	🤼🏽‍♀️	women wrestling: medium skin tone
People & Body	🤼🏾‍♀️	women wrestling: medium-dark skin tone
People & Body	🤼🏿‍♀️	women wrestling: dark skin tone
People & Body	🧑🏻‍🫯‍🧑🏼	people wrestling: light skin tone, medium-light skin tone
People & Body	🧑🏻‍🫯‍🧑🏽	people wrestling: light skin tone, medium skin tone
People & Body	🧑🏻‍🫯‍🧑🏾	people wrestling: light skin tone, medium-dark skin tone
People & Body	🧑🏻‍🫯‍🧑🏿	people wrestling: light skin tone, dark skin tone
People & Body	🧑🏼‍🫯‍🧑🏻	people wrestling: medium-light skin tone, light skin tone
        """,
        """
People & Body	🧑🏼‍🫯‍🧑🏽	people wrestling: medium-light skin tone, medium skin tone
People & Body	🧑🏼‍🫯‍🧑🏾	people wrestling: medium-light skin tone, medium-dark skin tone
People & Body	🧑🏼‍🫯‍🧑🏿	people wrestling: medium-light skin tone, dark skin tone
People & Body	🧑🏽‍🫯‍🧑🏻	people wrestling: medium skin tone, light skin tone
People & Body	🧑🏽‍🫯‍🧑🏼	people wrestling: medium skin tone, medium-light skin tone
People & Body	🧑🏽‍🫯‍🧑🏾	people wrestling: medium skin tone, medium-dark skin tone
People & Body	🧑🏽‍🫯‍🧑🏿	people wrestling: medium skin tone, dark skin tone
People & Body	🧑🏾‍🫯‍🧑🏻	people wrestling: medium-dark skin tone, light skin tone
People & Body	🧑🏾‍🫯‍🧑🏼	people wrestling: medium-dark skin tone, medium-light skin tone
People & Body	🧑🏾‍🫯‍🧑🏽	people wrestling: medium-dark skin tone, medium skin tone
People & Body	🧑🏾‍🫯‍🧑🏿	people wrestling: medium-dark skin tone, dark skin tone
People & Body	🧑🏿‍🫯‍🧑🏻	people wrestling: dark skin tone, light skin tone
People & Body	🧑🏿‍🫯‍🧑🏼	people wrestling: dark skin tone, medium-light skin tone
People & Body	🧑🏿‍🫯‍🧑🏽	people wrestling: dark skin tone, medium skin tone
People & Body	🧑🏿‍🫯‍🧑🏾	people wrestling: dark skin tone, medium-dark skin tone
People & Body	👨🏻‍🫯‍👨🏼	men wrestling: light skin tone, medium-light skin tone
People & Body	👨🏻‍🫯‍👨🏽	men wrestling: light skin tone, medium skin tone
People & Body	👨🏻‍🫯‍👨🏾	men wrestling: light skin tone, medium-dark skin tone
People & Body	👨🏻‍🫯‍👨🏿	men wrestling: light skin tone, dark skin tone
People & Body	👨🏼‍🫯‍👨🏻	men wrestling: medium-light skin tone, light skin tone
People & Body	👨🏼‍🫯‍👨🏽	men wrestling: medium-light skin tone, medium skin tone
People & Body	👨🏼‍🫯‍👨🏾	men wrestling: medium-light skin tone, medium-dark skin tone
People & Body	👨🏼‍🫯‍👨🏿	men wrestling: medium-light skin tone, dark skin tone
People & Body	👨🏽‍🫯‍👨🏻	men wrestling: medium skin tone, light skin tone
People & Body	👨🏽‍🫯‍👨🏼	men wrestling: medium skin tone, medium-light skin tone
People & Body	👨🏽‍🫯‍👨🏾	men wrestling: medium skin tone, medium-dark skin tone
People & Body	👨🏽‍🫯‍👨🏿	men wrestling: medium skin tone, dark skin tone
People & Body	👨🏾‍🫯‍👨🏻	men wrestling: medium-dark skin tone, light skin tone
People & Body	👨🏾‍🫯‍👨🏼	men wrestling: medium-dark skin tone, medium-light skin tone
People & Body	👨🏾‍🫯‍👨🏽	men wrestling: medium-dark skin tone, medium skin tone
People & Body	👨🏾‍🫯‍👨🏿	men wrestling: medium-dark skin tone, dark skin tone
People & Body	👨🏿‍🫯‍👨🏻	men wrestling: dark skin tone, light skin tone
People & Body	👨🏿‍🫯‍👨🏼	men wrestling: dark skin tone, medium-light skin tone
People & Body	👨🏿‍🫯‍👨🏽	men wrestling: dark skin tone, medium skin tone
People & Body	👨🏿‍🫯‍👨🏾	men wrestling: dark skin tone, medium-dark skin tone
People & Body	👩🏻‍🫯‍👩🏼	women wrestling: light skin tone, medium-light skin tone
People & Body	👩🏻‍🫯‍👩🏽	women wrestling: light skin tone, medium skin tone
People & Body	👩🏻‍🫯‍👩🏾	women wrestling: light skin tone, medium-dark skin tone
People & Body	👩🏻‍🫯‍👩🏿	women wrestling: light skin tone, dark skin tone
People & Body	👩🏼‍🫯‍👩🏻	women wrestling: medium-light skin tone, light skin tone
People & Body	👩🏼‍🫯‍👩🏽	women wrestling: medium-light skin tone, medium skin tone
People & Body	👩🏼‍🫯‍👩🏾	women wrestling: medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍🫯‍👩🏿	women wrestling: medium-light skin tone, dark skin tone
People & Body	👩🏽‍🫯‍👩🏻	women wrestling: medium skin tone, light skin tone
People & Body	👩🏽‍🫯‍👩🏼	women wrestling: medium skin tone, medium-light skin tone
People & Body	👩🏽‍🫯‍👩🏾	women wrestling: medium skin tone, medium-dark skin tone
People & Body	👩🏽‍🫯‍👩🏿	women wrestling: medium skin tone, dark skin tone
People & Body	👩🏾‍🫯‍👩🏻	women wrestling: medium-dark skin tone, light skin tone
People & Body	👩🏾‍🫯‍👩🏼	women wrestling: medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍🫯‍👩🏽	women wrestling: medium-dark skin tone, medium skin tone
People & Body	👩🏾‍🫯‍👩🏿	women wrestling: medium-dark skin tone, dark skin tone
People & Body	👩🏿‍🫯‍👩🏻	women wrestling: dark skin tone, light skin tone
People & Body	👩🏿‍🫯‍👩🏼	women wrestling: dark skin tone, medium-light skin tone
People & Body	👩🏿‍🫯‍👩🏽	women wrestling: dark skin tone, medium skin tone
People & Body	👩🏿‍🫯‍👩🏾	women wrestling: dark skin tone, medium-dark skin tone
People & Body	🤽	person playing water polo
People & Body	🤽🏻	person playing water polo: light skin tone
People & Body	🤽🏼	person playing water polo: medium-light skin tone
People & Body	🤽🏽	person playing water polo: medium skin tone
People & Body	🤽🏾	person playing water polo: medium-dark skin tone
People & Body	🤽🏿	person playing water polo: dark skin tone
People & Body	🤽‍♂️	man playing water polo
People & Body	🤽🏻‍♂️	man playing water polo: light skin tone
People & Body	🤽🏼‍♂️	man playing water polo: medium-light skin tone
People & Body	🤽🏽‍♂️	man playing water polo: medium skin tone
People & Body	🤽🏾‍♂️	man playing water polo: medium-dark skin tone
People & Body	🤽🏿‍♂️	man playing water polo: dark skin tone
People & Body	🤽‍♀️	woman playing water polo
People & Body	🤽🏻‍♀️	woman playing water polo: light skin tone
People & Body	🤽🏼‍♀️	woman playing water polo: medium-light skin tone
People & Body	🤽🏽‍♀️	woman playing water polo: medium skin tone
People & Body	🤽🏾‍♀️	woman playing water polo: medium-dark skin tone
People & Body	🤽🏿‍♀️	woman playing water polo: dark skin tone
People & Body	🤾	person playing handball
People & Body	🤾🏻	person playing handball: light skin tone
People & Body	🤾🏼	person playing handball: medium-light skin tone
People & Body	🤾🏽	person playing handball: medium skin tone
People & Body	🤾🏾	person playing handball: medium-dark skin tone
People & Body	🤾🏿	person playing handball: dark skin tone
People & Body	🤾‍♂️	man playing handball
People & Body	🤾🏻‍♂️	man playing handball: light skin tone
People & Body	🤾🏼‍♂️	man playing handball: medium-light skin tone
People & Body	🤾🏽‍♂️	man playing handball: medium skin tone
People & Body	🤾🏾‍♂️	man playing handball: medium-dark skin tone
People & Body	🤾🏿‍♂️	man playing handball: dark skin tone
People & Body	🤾‍♀️	woman playing handball
People & Body	🤾🏻‍♀️	woman playing handball: light skin tone
People & Body	🤾🏼‍♀️	woman playing handball: medium-light skin tone
People & Body	🤾🏽‍♀️	woman playing handball: medium skin tone
People & Body	🤾🏾‍♀️	woman playing handball: medium-dark skin tone
People & Body	🤾🏿‍♀️	woman playing handball: dark skin tone
People & Body	🤹	person juggling
People & Body	🤹🏻	person juggling: light skin tone
People & Body	🤹🏼	person juggling: medium-light skin tone
People & Body	🤹🏽	person juggling: medium skin tone
People & Body	🤹🏾	person juggling: medium-dark skin tone
People & Body	🤹🏿	person juggling: dark skin tone
People & Body	🤹‍♂️	man juggling
People & Body	🤹🏻‍♂️	man juggling: light skin tone
People & Body	🤹🏼‍♂️	man juggling: medium-light skin tone
People & Body	🤹🏽‍♂️	man juggling: medium skin tone
People & Body	🤹🏾‍♂️	man juggling: medium-dark skin tone
People & Body	🤹🏿‍♂️	man juggling: dark skin tone
People & Body	🤹‍♀️	woman juggling
People & Body	🤹🏻‍♀️	woman juggling: light skin tone
People & Body	🤹🏼‍♀️	woman juggling: medium-light skin tone
People & Body	🤹🏽‍♀️	woman juggling: medium skin tone
People & Body	🤹🏾‍♀️	woman juggling: medium-dark skin tone
People & Body	🤹🏿‍♀️	woman juggling: dark skin tone
People & Body	🧘	person in lotus position
People & Body	🧘🏻	person in lotus position: light skin tone
People & Body	🧘🏼	person in lotus position: medium-light skin tone
People & Body	🧘🏽	person in lotus position: medium skin tone
People & Body	🧘🏾	person in lotus position: medium-dark skin tone
People & Body	🧘🏿	person in lotus position: dark skin tone
People & Body	🧘‍♂️	man in lotus position
People & Body	🧘🏻‍♂️	man in lotus position: light skin tone
People & Body	🧘🏼‍♂️	man in lotus position: medium-light skin tone
People & Body	🧘🏽‍♂️	man in lotus position: medium skin tone
People & Body	🧘🏾‍♂️	man in lotus position: medium-dark skin tone
People & Body	🧘🏿‍♂️	man in lotus position: dark skin tone
People & Body	🧘‍♀️	woman in lotus position
People & Body	🧘🏻‍♀️	woman in lotus position: light skin tone
People & Body	🧘🏼‍♀️	woman in lotus position: medium-light skin tone
People & Body	🧘🏽‍♀️	woman in lotus position: medium skin tone
People & Body	🧘🏾‍♀️	woman in lotus position: medium-dark skin tone
People & Body	🧘🏿‍♀️	woman in lotus position: dark skin tone
People & Body	🛀	person taking bath
People & Body	🛀🏻	person taking bath: light skin tone
People & Body	🛀🏼	person taking bath: medium-light skin tone
People & Body	🛀🏽	person taking bath: medium skin tone
People & Body	🛀🏾	person taking bath: medium-dark skin tone
People & Body	🛀🏿	person taking bath: dark skin tone
People & Body	🛌	person in bed
People & Body	🛌🏻	person in bed: light skin tone
People & Body	🛌🏼	person in bed: medium-light skin tone
People & Body	🛌🏽	person in bed: medium skin tone
People & Body	🛌🏾	person in bed: medium-dark skin tone
People & Body	🛌🏿	person in bed: dark skin tone
People & Body	🧑‍🤝‍🧑	people holding hands
People & Body	🧑🏻‍🤝‍🧑🏻	people holding hands: light skin tone
People & Body	🧑🏻‍🤝‍🧑🏼	people holding hands: light skin tone, medium-light skin tone
People & Body	🧑🏻‍🤝‍🧑🏽	people holding hands: light skin tone, medium skin tone
People & Body	🧑🏻‍🤝‍🧑🏾	people holding hands: light skin tone, medium-dark skin tone
People & Body	🧑🏻‍🤝‍🧑🏿	people holding hands: light skin tone, dark skin tone
People & Body	🧑🏼‍🤝‍🧑🏻	people holding hands: medium-light skin tone, light skin tone
People & Body	🧑🏼‍🤝‍🧑🏼	people holding hands: medium-light skin tone
People & Body	🧑🏼‍🤝‍🧑🏽	people holding hands: medium-light skin tone, medium skin tone
People & Body	🧑🏼‍🤝‍🧑🏾	people holding hands: medium-light skin tone, medium-dark skin tone
People & Body	🧑🏼‍🤝‍🧑🏿	people holding hands: medium-light skin tone, dark skin tone
People & Body	🧑🏽‍🤝‍🧑🏻	people holding hands: medium skin tone, light skin tone
People & Body	🧑🏽‍🤝‍🧑🏼	people holding hands: medium skin tone, medium-light skin tone
People & Body	🧑🏽‍🤝‍🧑🏽	people holding hands: medium skin tone
People & Body	🧑🏽‍🤝‍🧑🏾	people holding hands: medium skin tone, medium-dark skin tone
People & Body	🧑🏽‍🤝‍🧑🏿	people holding hands: medium skin tone, dark skin tone
People & Body	🧑🏾‍🤝‍🧑🏻	people holding hands: medium-dark skin tone, light skin tone
People & Body	🧑🏾‍🤝‍🧑🏼	people holding hands: medium-dark skin tone, medium-light skin tone
People & Body	🧑🏾‍🤝‍🧑🏽	people holding hands: medium-dark skin tone, medium skin tone
People & Body	🧑🏾‍🤝‍🧑🏾	people holding hands: medium-dark skin tone
People & Body	🧑🏾‍🤝‍🧑🏿	people holding hands: medium-dark skin tone, dark skin tone
People & Body	🧑🏿‍🤝‍🧑🏻	people holding hands: dark skin tone, light skin tone
People & Body	🧑🏿‍🤝‍🧑🏼	people holding hands: dark skin tone, medium-light skin tone
People & Body	🧑🏿‍🤝‍🧑🏽	people holding hands: dark skin tone, medium skin tone
People & Body	🧑🏿‍🤝‍🧑🏾	people holding hands: dark skin tone, medium-dark skin tone
People & Body	🧑🏿‍🤝‍🧑🏿	people holding hands: dark skin tone
People & Body	👭	women holding hands
People & Body	👭🏻	women holding hands: light skin tone
People & Body	👩🏻‍🤝‍👩🏼	women holding hands: light skin tone, medium-light skin tone
People & Body	👩🏻‍🤝‍👩🏽	women holding hands: light skin tone, medium skin tone
People & Body	👩🏻‍🤝‍👩🏾	women holding hands: light skin tone, medium-dark skin tone
People & Body	👩🏻‍🤝‍👩🏿	women holding hands: light skin tone, dark skin tone
People & Body	👩🏼‍🤝‍👩🏻	women holding hands: medium-light skin tone, light skin tone
People & Body	👭🏼	women holding hands: medium-light skin tone
People & Body	👩🏼‍🤝‍👩🏽	women holding hands: medium-light skin tone, medium skin tone
People & Body	👩🏼‍🤝‍👩🏾	women holding hands: medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍🤝‍👩🏿	women holding hands: medium-light skin tone, dark skin tone
People & Body	👩🏽‍🤝‍👩🏻	women holding hands: medium skin tone, light skin tone
People & Body	👩🏽‍🤝‍👩🏼	women holding hands: medium skin tone, medium-light skin tone
People & Body	👭🏽	women holding hands: medium skin tone
People & Body	👩🏽‍🤝‍👩🏾	women holding hands: medium skin tone, medium-dark skin tone
People & Body	👩🏽‍🤝‍👩🏿	women holding hands: medium skin tone, dark skin tone
People & Body	👩🏾‍🤝‍👩🏻	women holding hands: medium-dark skin tone, light skin tone
People & Body	👩🏾‍🤝‍👩🏼	women holding hands: medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍🤝‍👩🏽	women holding hands: medium-dark skin tone, medium skin tone
People & Body	👭🏾	women holding hands: medium-dark skin tone
People & Body	👩🏾‍🤝‍👩🏿	women holding hands: medium-dark skin tone, dark skin tone
People & Body	👩🏿‍🤝‍👩🏻	women holding hands: dark skin tone, light skin tone
People & Body	👩🏿‍🤝‍👩🏼	women holding hands: dark skin tone, medium-light skin tone
People & Body	👩🏿‍🤝‍👩🏽	women holding hands: dark skin tone, medium skin tone
People & Body	👩🏿‍🤝‍👩🏾	women holding hands: dark skin tone, medium-dark skin tone
People & Body	👭🏿	women holding hands: dark skin tone
People & Body	👫	woman and man holding hands
People & Body	👫🏻	woman and man holding hands: light skin tone
People & Body	👩🏻‍🤝‍👨🏼	woman and man holding hands: light skin tone, medium-light skin tone
People & Body	👩🏻‍🤝‍👨🏽	woman and man holding hands: light skin tone, medium skin tone
People & Body	👩🏻‍🤝‍👨🏾	woman and man holding hands: light skin tone, medium-dark skin tone
People & Body	👩🏻‍🤝‍👨🏿	woman and man holding hands: light skin tone, dark skin tone
People & Body	👩🏼‍🤝‍👨🏻	woman and man holding hands: medium-light skin tone, light skin tone
People & Body	👫🏼	woman and man holding hands: medium-light skin tone
People & Body	👩🏼‍🤝‍👨🏽	woman and man holding hands: medium-light skin tone, medium skin tone
People & Body	👩🏼‍🤝‍👨🏾	woman and man holding hands: medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍🤝‍👨🏿	woman and man holding hands: medium-light skin tone, dark skin tone
People & Body	👩🏽‍🤝‍👨🏻	woman and man holding hands: medium skin tone, light skin tone
People & Body	👩🏽‍🤝‍👨🏼	woman and man holding hands: medium skin tone, medium-light skin tone
People & Body	👫🏽	woman and man holding hands: medium skin tone
People & Body	👩🏽‍🤝‍👨🏾	woman and man holding hands: medium skin tone, medium-dark skin tone
People & Body	👩🏽‍🤝‍👨🏿	woman and man holding hands: medium skin tone, dark skin tone
People & Body	👩🏾‍🤝‍👨🏻	woman and man holding hands: medium-dark skin tone, light skin tone
People & Body	👩🏾‍🤝‍👨🏼	woman and man holding hands: medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍🤝‍👨🏽	woman and man holding hands: medium-dark skin tone, medium skin tone
People & Body	👫🏾	woman and man holding hands: medium-dark skin tone
People & Body	👩🏾‍🤝‍👨🏿	woman and man holding hands: medium-dark skin tone, dark skin tone
People & Body	👩🏿‍🤝‍👨🏻	woman and man holding hands: dark skin tone, light skin tone
People & Body	👩🏿‍🤝‍👨🏼	woman and man holding hands: dark skin tone, medium-light skin tone
People & Body	👩🏿‍🤝‍👨🏽	woman and man holding hands: dark skin tone, medium skin tone
        """,
        """
People & Body	👩🏿‍🤝‍👨🏾	woman and man holding hands: dark skin tone, medium-dark skin tone
People & Body	👫🏿	woman and man holding hands: dark skin tone
People & Body	👬	men holding hands
People & Body	👬🏻	men holding hands: light skin tone
People & Body	👨🏻‍🤝‍👨🏼	men holding hands: light skin tone, medium-light skin tone
People & Body	👨🏻‍🤝‍👨🏽	men holding hands: light skin tone, medium skin tone
People & Body	👨🏻‍🤝‍👨🏾	men holding hands: light skin tone, medium-dark skin tone
People & Body	👨🏻‍🤝‍👨🏿	men holding hands: light skin tone, dark skin tone
People & Body	👨🏼‍🤝‍👨🏻	men holding hands: medium-light skin tone, light skin tone
People & Body	👬🏼	men holding hands: medium-light skin tone
People & Body	👨🏼‍🤝‍👨🏽	men holding hands: medium-light skin tone, medium skin tone
People & Body	👨🏼‍🤝‍👨🏾	men holding hands: medium-light skin tone, medium-dark skin tone
People & Body	👨🏼‍🤝‍👨🏿	men holding hands: medium-light skin tone, dark skin tone
People & Body	👨🏽‍🤝‍👨🏻	men holding hands: medium skin tone, light skin tone
People & Body	👨🏽‍🤝‍👨🏼	men holding hands: medium skin tone, medium-light skin tone
People & Body	👬🏽	men holding hands: medium skin tone
People & Body	👨🏽‍🤝‍👨🏾	men holding hands: medium skin tone, medium-dark skin tone
People & Body	👨🏽‍🤝‍👨🏿	men holding hands: medium skin tone, dark skin tone
People & Body	👨🏾‍🤝‍👨🏻	men holding hands: medium-dark skin tone, light skin tone
People & Body	👨🏾‍🤝‍👨🏼	men holding hands: medium-dark skin tone, medium-light skin tone
People & Body	👨🏾‍🤝‍👨🏽	men holding hands: medium-dark skin tone, medium skin tone
People & Body	👬🏾	men holding hands: medium-dark skin tone
People & Body	👨🏾‍🤝‍👨🏿	men holding hands: medium-dark skin tone, dark skin tone
People & Body	👨🏿‍🤝‍👨🏻	men holding hands: dark skin tone, light skin tone
People & Body	👨🏿‍🤝‍👨🏼	men holding hands: dark skin tone, medium-light skin tone
People & Body	👨🏿‍🤝‍👨🏽	men holding hands: dark skin tone, medium skin tone
People & Body	👨🏿‍🤝‍👨🏾	men holding hands: dark skin tone, medium-dark skin tone
People & Body	👬🏿	men holding hands: dark skin tone
People & Body	💏	kiss
People & Body	💏🏻	kiss: light skin tone
People & Body	💏🏼	kiss: medium-light skin tone
People & Body	💏🏽	kiss: medium skin tone
People & Body	💏🏾	kiss: medium-dark skin tone
People & Body	💏🏿	kiss: dark skin tone
People & Body	🧑🏻‍❤️‍💋‍🧑🏼	kiss: person, person, light skin tone, medium-light skin tone
People & Body	🧑🏻‍❤️‍💋‍🧑🏽	kiss: person, person, light skin tone, medium skin tone
People & Body	🧑🏻‍❤️‍💋‍🧑🏾	kiss: person, person, light skin tone, medium-dark skin tone
People & Body	🧑🏻‍❤️‍💋‍🧑🏿	kiss: person, person, light skin tone, dark skin tone
People & Body	🧑🏼‍❤️‍💋‍🧑🏻	kiss: person, person, medium-light skin tone, light skin tone
People & Body	🧑🏼‍❤️‍💋‍🧑🏽	kiss: person, person, medium-light skin tone, medium skin tone
People & Body	🧑🏼‍❤️‍💋‍🧑🏾	kiss: person, person, medium-light skin tone, medium-dark skin tone
People & Body	🧑🏼‍❤️‍💋‍🧑🏿	kiss: person, person, medium-light skin tone, dark skin tone
People & Body	🧑🏽‍❤️‍💋‍🧑🏻	kiss: person, person, medium skin tone, light skin tone
People & Body	🧑🏽‍❤️‍💋‍🧑🏼	kiss: person, person, medium skin tone, medium-light skin tone
People & Body	🧑🏽‍❤️‍💋‍🧑🏾	kiss: person, person, medium skin tone, medium-dark skin tone
People & Body	🧑🏽‍❤️‍💋‍🧑🏿	kiss: person, person, medium skin tone, dark skin tone
People & Body	🧑🏾‍❤️‍💋‍🧑🏻	kiss: person, person, medium-dark skin tone, light skin tone
People & Body	🧑🏾‍❤️‍💋‍🧑🏼	kiss: person, person, medium-dark skin tone, medium-light skin tone
People & Body	🧑🏾‍❤️‍💋‍🧑🏽	kiss: person, person, medium-dark skin tone, medium skin tone
People & Body	🧑🏾‍❤️‍💋‍🧑🏿	kiss: person, person, medium-dark skin tone, dark skin tone
People & Body	🧑🏿‍❤️‍💋‍🧑🏻	kiss: person, person, dark skin tone, light skin tone
People & Body	🧑🏿‍❤️‍💋‍🧑🏼	kiss: person, person, dark skin tone, medium-light skin tone
People & Body	🧑🏿‍❤️‍💋‍🧑🏽	kiss: person, person, dark skin tone, medium skin tone
People & Body	🧑🏿‍❤️‍💋‍🧑🏾	kiss: person, person, dark skin tone, medium-dark skin tone
People & Body	👩‍❤️‍💋‍👨	kiss: woman, man
People & Body	👩🏻‍❤️‍💋‍👨🏻	kiss: woman, man, light skin tone
People & Body	👩🏻‍❤️‍💋‍👨🏼	kiss: woman, man, light skin tone, medium-light skin tone
People & Body	👩🏻‍❤️‍💋‍👨🏽	kiss: woman, man, light skin tone, medium skin tone
People & Body	👩🏻‍❤️‍💋‍👨🏾	kiss: woman, man, light skin tone, medium-dark skin tone
People & Body	👩🏻‍❤️‍💋‍👨🏿	kiss: woman, man, light skin tone, dark skin tone
People & Body	👩🏼‍❤️‍💋‍👨🏻	kiss: woman, man, medium-light skin tone, light skin tone
People & Body	👩🏼‍❤️‍💋‍👨🏼	kiss: woman, man, medium-light skin tone
People & Body	👩🏼‍❤️‍💋‍👨🏽	kiss: woman, man, medium-light skin tone, medium skin tone
People & Body	👩🏼‍❤️‍💋‍👨🏾	kiss: woman, man, medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍❤️‍💋‍👨🏿	kiss: woman, man, medium-light skin tone, dark skin tone
People & Body	👩🏽‍❤️‍💋‍👨🏻	kiss: woman, man, medium skin tone, light skin tone
People & Body	👩🏽‍❤️‍💋‍👨🏼	kiss: woman, man, medium skin tone, medium-light skin tone
People & Body	👩🏽‍❤️‍💋‍👨🏽	kiss: woman, man, medium skin tone
People & Body	👩🏽‍❤️‍💋‍👨🏾	kiss: woman, man, medium skin tone, medium-dark skin tone
People & Body	👩🏽‍❤️‍💋‍👨🏿	kiss: woman, man, medium skin tone, dark skin tone
People & Body	👩🏾‍❤️‍💋‍👨🏻	kiss: woman, man, medium-dark skin tone, light skin tone
People & Body	👩🏾‍❤️‍💋‍👨🏼	kiss: woman, man, medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍❤️‍💋‍👨🏽	kiss: woman, man, medium-dark skin tone, medium skin tone
People & Body	👩🏾‍❤️‍💋‍👨🏾	kiss: woman, man, medium-dark skin tone
People & Body	👩🏾‍❤️‍💋‍👨🏿	kiss: woman, man, medium-dark skin tone, dark skin tone
People & Body	👩🏿‍❤️‍💋‍👨🏻	kiss: woman, man, dark skin tone, light skin tone
People & Body	👩🏿‍❤️‍💋‍👨🏼	kiss: woman, man, dark skin tone, medium-light skin tone
People & Body	👩🏿‍❤️‍💋‍👨🏽	kiss: woman, man, dark skin tone, medium skin tone
People & Body	👩🏿‍❤️‍💋‍👨🏾	kiss: woman, man, dark skin tone, medium-dark skin tone
People & Body	👩🏿‍❤️‍💋‍👨🏿	kiss: woman, man, dark skin tone
People & Body	👨‍❤️‍💋‍👨	kiss: man, man
People & Body	👨🏻‍❤️‍💋‍👨🏻	kiss: man, man, light skin tone
People & Body	👨🏻‍❤️‍💋‍👨🏼	kiss: man, man, light skin tone, medium-light skin tone
People & Body	👨🏻‍❤️‍💋‍👨🏽	kiss: man, man, light skin tone, medium skin tone
People & Body	👨🏻‍❤️‍💋‍👨🏾	kiss: man, man, light skin tone, medium-dark skin tone
People & Body	👨🏻‍❤️‍💋‍👨🏿	kiss: man, man, light skin tone, dark skin tone
People & Body	👨🏼‍❤️‍💋‍👨🏻	kiss: man, man, medium-light skin tone, light skin tone
People & Body	👨🏼‍❤️‍💋‍👨🏼	kiss: man, man, medium-light skin tone
People & Body	👨🏼‍❤️‍💋‍👨🏽	kiss: man, man, medium-light skin tone, medium skin tone
People & Body	👨🏼‍❤️‍💋‍👨🏾	kiss: man, man, medium-light skin tone, medium-dark skin tone
People & Body	👨🏼‍❤️‍💋‍👨🏿	kiss: man, man, medium-light skin tone, dark skin tone
People & Body	👨🏽‍❤️‍💋‍👨🏻	kiss: man, man, medium skin tone, light skin tone
People & Body	👨🏽‍❤️‍💋‍👨🏼	kiss: man, man, medium skin tone, medium-light skin tone
People & Body	👨🏽‍❤️‍💋‍👨🏽	kiss: man, man, medium skin tone
People & Body	👨🏽‍❤️‍💋‍👨🏾	kiss: man, man, medium skin tone, medium-dark skin tone
People & Body	👨🏽‍❤️‍💋‍👨🏿	kiss: man, man, medium skin tone, dark skin tone
People & Body	👨🏾‍❤️‍💋‍👨🏻	kiss: man, man, medium-dark skin tone, light skin tone
People & Body	👨🏾‍❤️‍💋‍👨🏼	kiss: man, man, medium-dark skin tone, medium-light skin tone
People & Body	👨🏾‍❤️‍💋‍👨🏽	kiss: man, man, medium-dark skin tone, medium skin tone
People & Body	👨🏾‍❤️‍💋‍👨🏾	kiss: man, man, medium-dark skin tone
People & Body	👨🏾‍❤️‍💋‍👨🏿	kiss: man, man, medium-dark skin tone, dark skin tone
People & Body	👨🏿‍❤️‍💋‍👨🏻	kiss: man, man, dark skin tone, light skin tone
People & Body	👨🏿‍❤️‍💋‍👨🏼	kiss: man, man, dark skin tone, medium-light skin tone
People & Body	👨🏿‍❤️‍💋‍👨🏽	kiss: man, man, dark skin tone, medium skin tone
People & Body	👨🏿‍❤️‍💋‍👨🏾	kiss: man, man, dark skin tone, medium-dark skin tone
People & Body	👨🏿‍❤️‍💋‍👨🏿	kiss: man, man, dark skin tone
People & Body	👩‍❤️‍💋‍👩	kiss: woman, woman
People & Body	👩🏻‍❤️‍💋‍👩🏻	kiss: woman, woman, light skin tone
People & Body	👩🏻‍❤️‍💋‍👩🏼	kiss: woman, woman, light skin tone, medium-light skin tone
People & Body	👩🏻‍❤️‍💋‍👩🏽	kiss: woman, woman, light skin tone, medium skin tone
People & Body	👩🏻‍❤️‍💋‍👩🏾	kiss: woman, woman, light skin tone, medium-dark skin tone
People & Body	👩🏻‍❤️‍💋‍👩🏿	kiss: woman, woman, light skin tone, dark skin tone
People & Body	👩🏼‍❤️‍💋‍👩🏻	kiss: woman, woman, medium-light skin tone, light skin tone
People & Body	👩🏼‍❤️‍💋‍👩🏼	kiss: woman, woman, medium-light skin tone
People & Body	👩🏼‍❤️‍💋‍👩🏽	kiss: woman, woman, medium-light skin tone, medium skin tone
People & Body	👩🏼‍❤️‍💋‍👩🏾	kiss: woman, woman, medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍❤️‍💋‍👩🏿	kiss: woman, woman, medium-light skin tone, dark skin tone
People & Body	👩🏽‍❤️‍💋‍👩🏻	kiss: woman, woman, medium skin tone, light skin tone
People & Body	👩🏽‍❤️‍💋‍👩🏼	kiss: woman, woman, medium skin tone, medium-light skin tone
People & Body	👩🏽‍❤️‍💋‍👩🏽	kiss: woman, woman, medium skin tone
People & Body	👩🏽‍❤️‍💋‍👩🏾	kiss: woman, woman, medium skin tone, medium-dark skin tone
People & Body	👩🏽‍❤️‍💋‍👩🏿	kiss: woman, woman, medium skin tone, dark skin tone
People & Body	👩🏾‍❤️‍💋‍👩🏻	kiss: woman, woman, medium-dark skin tone, light skin tone
People & Body	👩🏾‍❤️‍💋‍👩🏼	kiss: woman, woman, medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍❤️‍💋‍👩🏽	kiss: woman, woman, medium-dark skin tone, medium skin tone
People & Body	👩🏾‍❤️‍💋‍👩🏾	kiss: woman, woman, medium-dark skin tone
People & Body	👩🏾‍❤️‍💋‍👩🏿	kiss: woman, woman, medium-dark skin tone, dark skin tone
People & Body	👩🏿‍❤️‍💋‍👩🏻	kiss: woman, woman, dark skin tone, light skin tone
People & Body	👩🏿‍❤️‍💋‍👩🏼	kiss: woman, woman, dark skin tone, medium-light skin tone
People & Body	👩🏿‍❤️‍💋‍👩🏽	kiss: woman, woman, dark skin tone, medium skin tone
People & Body	👩🏿‍❤️‍💋‍👩🏾	kiss: woman, woman, dark skin tone, medium-dark skin tone
People & Body	👩🏿‍❤️‍💋‍👩🏿	kiss: woman, woman, dark skin tone
People & Body	💑	couple with heart
People & Body	💑🏻	couple with heart: light skin tone
People & Body	💑🏼	couple with heart: medium-light skin tone
People & Body	💑🏽	couple with heart: medium skin tone
People & Body	💑🏾	couple with heart: medium-dark skin tone
People & Body	💑🏿	couple with heart: dark skin tone
People & Body	🧑🏻‍❤️‍🧑🏼	couple with heart: person, person, light skin tone, medium-light skin tone
People & Body	🧑🏻‍❤️‍🧑🏽	couple with heart: person, person, light skin tone, medium skin tone
People & Body	🧑🏻‍❤️‍🧑🏾	couple with heart: person, person, light skin tone, medium-dark skin tone
People & Body	🧑🏻‍❤️‍🧑🏿	couple with heart: person, person, light skin tone, dark skin tone
People & Body	🧑🏼‍❤️‍🧑🏻	couple with heart: person, person, medium-light skin tone, light skin tone
People & Body	🧑🏼‍❤️‍🧑🏽	couple with heart: person, person, medium-light skin tone, medium skin tone
People & Body	🧑🏼‍❤️‍🧑🏾	couple with heart: person, person, medium-light skin tone, medium-dark skin tone
People & Body	🧑🏼‍❤️‍🧑🏿	couple with heart: person, person, medium-light skin tone, dark skin tone
People & Body	🧑🏽‍❤️‍🧑🏻	couple with heart: person, person, medium skin tone, light skin tone
People & Body	🧑🏽‍❤️‍🧑🏼	couple with heart: person, person, medium skin tone, medium-light skin tone
People & Body	🧑🏽‍❤️‍🧑🏾	couple with heart: person, person, medium skin tone, medium-dark skin tone
People & Body	🧑🏽‍❤️‍🧑🏿	couple with heart: person, person, medium skin tone, dark skin tone
People & Body	🧑🏾‍❤️‍🧑🏻	couple with heart: person, person, medium-dark skin tone, light skin tone
People & Body	🧑🏾‍❤️‍🧑🏼	couple with heart: person, person, medium-dark skin tone, medium-light skin tone
People & Body	🧑🏾‍❤️‍🧑🏽	couple with heart: person, person, medium-dark skin tone, medium skin tone
People & Body	🧑🏾‍❤️‍🧑🏿	couple with heart: person, person, medium-dark skin tone, dark skin tone
People & Body	🧑🏿‍❤️‍🧑🏻	couple with heart: person, person, dark skin tone, light skin tone
People & Body	🧑🏿‍❤️‍🧑🏼	couple with heart: person, person, dark skin tone, medium-light skin tone
People & Body	🧑🏿‍❤️‍🧑🏽	couple with heart: person, person, dark skin tone, medium skin tone
People & Body	🧑🏿‍❤️‍🧑🏾	couple with heart: person, person, dark skin tone, medium-dark skin tone
People & Body	👩‍❤️‍👨	couple with heart: woman, man
People & Body	👩🏻‍❤️‍👨🏻	couple with heart: woman, man, light skin tone
People & Body	👩🏻‍❤️‍👨🏼	couple with heart: woman, man, light skin tone, medium-light skin tone
People & Body	👩🏻‍❤️‍👨🏽	couple with heart: woman, man, light skin tone, medium skin tone
People & Body	👩🏻‍❤️‍👨🏾	couple with heart: woman, man, light skin tone, medium-dark skin tone
People & Body	👩🏻‍❤️‍👨🏿	couple with heart: woman, man, light skin tone, dark skin tone
People & Body	👩🏼‍❤️‍👨🏻	couple with heart: woman, man, medium-light skin tone, light skin tone
People & Body	👩🏼‍❤️‍👨🏼	couple with heart: woman, man, medium-light skin tone
People & Body	👩🏼‍❤️‍👨🏽	couple with heart: woman, man, medium-light skin tone, medium skin tone
People & Body	👩🏼‍❤️‍👨🏾	couple with heart: woman, man, medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍❤️‍👨🏿	couple with heart: woman, man, medium-light skin tone, dark skin tone
People & Body	👩🏽‍❤️‍👨🏻	couple with heart: woman, man, medium skin tone, light skin tone
People & Body	👩🏽‍❤️‍👨🏼	couple with heart: woman, man, medium skin tone, medium-light skin tone
People & Body	👩🏽‍❤️‍👨🏽	couple with heart: woman, man, medium skin tone
People & Body	👩🏽‍❤️‍👨🏾	couple with heart: woman, man, medium skin tone, medium-dark skin tone
People & Body	👩🏽‍❤️‍👨🏿	couple with heart: woman, man, medium skin tone, dark skin tone
People & Body	👩🏾‍❤️‍👨🏻	couple with heart: woman, man, medium-dark skin tone, light skin tone
People & Body	👩🏾‍❤️‍👨🏼	couple with heart: woman, man, medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍❤️‍👨🏽	couple with heart: woman, man, medium-dark skin tone, medium skin tone
People & Body	👩🏾‍❤️‍👨🏾	couple with heart: woman, man, medium-dark skin tone
People & Body	👩🏾‍❤️‍👨🏿	couple with heart: woman, man, medium-dark skin tone, dark skin tone
People & Body	👩🏿‍❤️‍👨🏻	couple with heart: woman, man, dark skin tone, light skin tone
People & Body	👩🏿‍❤️‍👨🏼	couple with heart: woman, man, dark skin tone, medium-light skin tone
        """,
        """
People & Body	👩🏿‍❤️‍👨🏽	couple with heart: woman, man, dark skin tone, medium skin tone
People & Body	👩🏿‍❤️‍👨🏾	couple with heart: woman, man, dark skin tone, medium-dark skin tone
People & Body	👩🏿‍❤️‍👨🏿	couple with heart: woman, man, dark skin tone
People & Body	👨‍❤️‍👨	couple with heart: man, man
People & Body	👨🏻‍❤️‍👨🏻	couple with heart: man, man, light skin tone
People & Body	👨🏻‍❤️‍👨🏼	couple with heart: man, man, light skin tone, medium-light skin tone
People & Body	👨🏻‍❤️‍👨🏽	couple with heart: man, man, light skin tone, medium skin tone
People & Body	👨🏻‍❤️‍👨🏾	couple with heart: man, man, light skin tone, medium-dark skin tone
People & Body	👨🏻‍❤️‍👨🏿	couple with heart: man, man, light skin tone, dark skin tone
People & Body	👨🏼‍❤️‍👨🏻	couple with heart: man, man, medium-light skin tone, light skin tone
People & Body	👨🏼‍❤️‍👨🏼	couple with heart: man, man, medium-light skin tone
People & Body	👨🏼‍❤️‍👨🏽	couple with heart: man, man, medium-light skin tone, medium skin tone
People & Body	👨🏼‍❤️‍👨🏾	couple with heart: man, man, medium-light skin tone, medium-dark skin tone
People & Body	👨🏼‍❤️‍👨🏿	couple with heart: man, man, medium-light skin tone, dark skin tone
People & Body	👨🏽‍❤️‍👨🏻	couple with heart: man, man, medium skin tone, light skin tone
People & Body	👨🏽‍❤️‍👨🏼	couple with heart: man, man, medium skin tone, medium-light skin tone
People & Body	👨🏽‍❤️‍👨🏽	couple with heart: man, man, medium skin tone
People & Body	👨🏽‍❤️‍👨🏾	couple with heart: man, man, medium skin tone, medium-dark skin tone
People & Body	👨🏽‍❤️‍👨🏿	couple with heart: man, man, medium skin tone, dark skin tone
People & Body	👨🏾‍❤️‍👨🏻	couple with heart: man, man, medium-dark skin tone, light skin tone
People & Body	👨🏾‍❤️‍👨🏼	couple with heart: man, man, medium-dark skin tone, medium-light skin tone
People & Body	👨🏾‍❤️‍👨🏽	couple with heart: man, man, medium-dark skin tone, medium skin tone
People & Body	👨🏾‍❤️‍👨🏾	couple with heart: man, man, medium-dark skin tone
People & Body	👨🏾‍❤️‍👨🏿	couple with heart: man, man, medium-dark skin tone, dark skin tone
People & Body	👨🏿‍❤️‍👨🏻	couple with heart: man, man, dark skin tone, light skin tone
People & Body	👨🏿‍❤️‍👨🏼	couple with heart: man, man, dark skin tone, medium-light skin tone
People & Body	👨🏿‍❤️‍👨🏽	couple with heart: man, man, dark skin tone, medium skin tone
People & Body	👨🏿‍❤️‍👨🏾	couple with heart: man, man, dark skin tone, medium-dark skin tone
People & Body	👨🏿‍❤️‍👨🏿	couple with heart: man, man, dark skin tone
People & Body	👩‍❤️‍👩	couple with heart: woman, woman
People & Body	👩🏻‍❤️‍👩🏻	couple with heart: woman, woman, light skin tone
People & Body	👩🏻‍❤️‍👩🏼	couple with heart: woman, woman, light skin tone, medium-light skin tone
People & Body	👩🏻‍❤️‍👩🏽	couple with heart: woman, woman, light skin tone, medium skin tone
People & Body	👩🏻‍❤️‍👩🏾	couple with heart: woman, woman, light skin tone, medium-dark skin tone
People & Body	👩🏻‍❤️‍👩🏿	couple with heart: woman, woman, light skin tone, dark skin tone
People & Body	👩🏼‍❤️‍👩🏻	couple with heart: woman, woman, medium-light skin tone, light skin tone
People & Body	👩🏼‍❤️‍👩🏼	couple with heart: woman, woman, medium-light skin tone
People & Body	👩🏼‍❤️‍👩🏽	couple with heart: woman, woman, medium-light skin tone, medium skin tone
People & Body	👩🏼‍❤️‍👩🏾	couple with heart: woman, woman, medium-light skin tone, medium-dark skin tone
People & Body	👩🏼‍❤️‍👩🏿	couple with heart: woman, woman, medium-light skin tone, dark skin tone
People & Body	👩🏽‍❤️‍👩🏻	couple with heart: woman, woman, medium skin tone, light skin tone
People & Body	👩🏽‍❤️‍👩🏼	couple with heart: woman, woman, medium skin tone, medium-light skin tone
People & Body	👩🏽‍❤️‍👩🏽	couple with heart: woman, woman, medium skin tone
People & Body	👩🏽‍❤️‍👩🏾	couple with heart: woman, woman, medium skin tone, medium-dark skin tone
People & Body	👩🏽‍❤️‍👩🏿	couple with heart: woman, woman, medium skin tone, dark skin tone
People & Body	👩🏾‍❤️‍👩🏻	couple with heart: woman, woman, medium-dark skin tone, light skin tone
People & Body	👩🏾‍❤️‍👩🏼	couple with heart: woman, woman, medium-dark skin tone, medium-light skin tone
People & Body	👩🏾‍❤️‍👩🏽	couple with heart: woman, woman, medium-dark skin tone, medium skin tone
People & Body	👩🏾‍❤️‍👩🏾	couple with heart: woman, woman, medium-dark skin tone
People & Body	👩🏾‍❤️‍👩🏿	couple with heart: woman, woman, medium-dark skin tone, dark skin tone
People & Body	👩🏿‍❤️‍👩🏻	couple with heart: woman, woman, dark skin tone, light skin tone
People & Body	👩🏿‍❤️‍👩🏼	couple with heart: woman, woman, dark skin tone, medium-light skin tone
People & Body	👩🏿‍❤️‍👩🏽	couple with heart: woman, woman, dark skin tone, medium skin tone
People & Body	👩🏿‍❤️‍👩🏾	couple with heart: woman, woman, dark skin tone, medium-dark skin tone
People & Body	👩🏿‍❤️‍👩🏿	couple with heart: woman, woman, dark skin tone
People & Body	👨‍👩‍👦	family: man, woman, boy
People & Body	👨‍👩‍👧	family: man, woman, girl
People & Body	👨‍👩‍👧‍👦	family: man, woman, girl, boy
People & Body	👨‍👩‍👦‍👦	family: man, woman, boy, boy
People & Body	👨‍👩‍👧‍👧	family: man, woman, girl, girl
People & Body	👨‍👨‍👦	family: man, man, boy
People & Body	👨‍👨‍👧	family: man, man, girl
People & Body	👨‍👨‍👧‍👦	family: man, man, girl, boy
People & Body	👨‍👨‍👦‍👦	family: man, man, boy, boy
People & Body	👨‍👨‍👧‍👧	family: man, man, girl, girl
People & Body	👩‍👩‍👦	family: woman, woman, boy
People & Body	👩‍👩‍👧	family: woman, woman, girl
People & Body	👩‍👩‍👧‍👦	family: woman, woman, girl, boy
People & Body	👩‍👩‍👦‍👦	family: woman, woman, boy, boy
People & Body	👩‍👩‍👧‍👧	family: woman, woman, girl, girl
People & Body	👨‍👦	family: man, boy
People & Body	👨‍👦‍👦	family: man, boy, boy
People & Body	👨‍👧	family: man, girl
People & Body	👨‍👧‍👦	family: man, girl, boy
People & Body	👨‍👧‍👧	family: man, girl, girl
People & Body	👩‍👦	family: woman, boy
People & Body	👩‍👦‍👦	family: woman, boy, boy
People & Body	👩‍👧	family: woman, girl
People & Body	👩‍👧‍👦	family: woman, girl, boy
People & Body	👩‍👧‍👧	family: woman, girl, girl
People & Body	🗣️	speaking head
People & Body	👤	bust in silhouette
People & Body	👥	busts in silhouette
People & Body	🫂	people hugging
People & Body	👪	family
People & Body	🧑‍🧑‍🧒	family: adult, adult, child
People & Body	🧑‍🧑‍🧒‍🧒	family: adult, adult, child, child
People & Body	🧑‍🧒	family: adult, child
People & Body	🧑‍🧒‍🧒	family: adult, child, child
People & Body	👣	footprints
People & Body	🫆	fingerprint
Animals & Nature	🐵	monkey face
Animals & Nature	🐒	monkey
Animals & Nature	🦍	gorilla
Animals & Nature	🦧	orangutan
Animals & Nature	🐶	dog face
Animals & Nature	🐕	dog
Animals & Nature	🦮	guide dog
Animals & Nature	🐕‍🦺	service dog
Animals & Nature	🐩	poodle
Animals & Nature	🐺	wolf
Animals & Nature	🦊	fox
Animals & Nature	🦝	raccoon
Animals & Nature	🐱	cat face
Animals & Nature	🐈	cat
Animals & Nature	🐈‍⬛	black cat
Animals & Nature	🦁	lion
Animals & Nature	🐯	tiger face
Animals & Nature	🐅	tiger
Animals & Nature	🐆	leopard
Animals & Nature	🐴	horse face
Animals & Nature	🫎	moose
Animals & Nature	🫏	donkey
Animals & Nature	🐎	horse
Animals & Nature	🦄	unicorn
Animals & Nature	🦓	zebra
Animals & Nature	🦌	deer
Animals & Nature	🦬	bison
Animals & Nature	🐮	cow face
Animals & Nature	🐂	ox
Animals & Nature	🐃	water buffalo
Animals & Nature	🐄	cow
Animals & Nature	🐷	pig face
Animals & Nature	🐖	pig
Animals & Nature	🐗	boar
Animals & Nature	🐽	pig nose
Animals & Nature	🐏	ram
Animals & Nature	🐑	ewe
Animals & Nature	🐐	goat
Animals & Nature	🐪	camel
Animals & Nature	🐫	two-hump camel
Animals & Nature	🦙	llama
Animals & Nature	🦒	giraffe
Animals & Nature	🐘	elephant
Animals & Nature	🦣	mammoth
Animals & Nature	🦏	rhinoceros
Animals & Nature	🦛	hippopotamus
Animals & Nature	🐭	mouse face
Animals & Nature	🐁	mouse
Animals & Nature	🐀	rat
Animals & Nature	🐹	hamster
Animals & Nature	🐰	rabbit face
Animals & Nature	🐇	rabbit
Animals & Nature	🐿️	chipmunk
Animals & Nature	🦫	beaver
Animals & Nature	🦔	hedgehog
Animals & Nature	🦇	bat
Animals & Nature	🐻	bear
Animals & Nature	🐻‍❄️	polar bear
Animals & Nature	🐨	koala
Animals & Nature	🐼	panda
Animals & Nature	🦥	sloth
Animals & Nature	🦦	otter
Animals & Nature	🦨	skunk
Animals & Nature	🦘	kangaroo
Animals & Nature	🦡	badger
Animals & Nature	🐾	paw prints
Animals & Nature	🦃	turkey
Animals & Nature	🐔	chicken
Animals & Nature	🐓	rooster
Animals & Nature	🐣	hatching chick
Animals & Nature	🐤	baby chick
Animals & Nature	🐥	front-facing baby chick
Animals & Nature	🐦	bird
Animals & Nature	🐧	penguin
Animals & Nature	🕊️	dove
Animals & Nature	🦅	eagle
Animals & Nature	🦆	duck
Animals & Nature	🦢	swan
Animals & Nature	🦉	owl
Animals & Nature	🦤	dodo
Animals & Nature	🪶	feather
Animals & Nature	🦩	flamingo
Animals & Nature	🦚	peacock
Animals & Nature	🦜	parrot
Animals & Nature	🪽	wing
Animals & Nature	🐦‍⬛	black bird
Animals & Nature	🪿	goose
Animals & Nature	🐦‍🔥	phoenix
Animals & Nature	🐸	frog
Animals & Nature	🐊	crocodile
Animals & Nature	🐢	turtle
Animals & Nature	🦎	lizard
Animals & Nature	🐍	snake
Animals & Nature	🐲	dragon face
Animals & Nature	🐉	dragon
Animals & Nature	🦕	sauropod
Animals & Nature	🦖	T-Rex
Animals & Nature	🐳	spouting whale
Animals & Nature	🐋	whale
Animals & Nature	🐬	dolphin
Animals & Nature	🫍	orca
Animals & Nature	🦭	seal
Animals & Nature	🐟	fish
Animals & Nature	🐠	tropical fish
Animals & Nature	🐡	blowfish
Animals & Nature	🦈	shark
Animals & Nature	🐙	octopus
Animals & Nature	🐚	spiral shell
Animals & Nature	🪸	coral
Animals & Nature	🪼	jellyfish
Animals & Nature	🦀	crab
Animals & Nature	🦞	lobster
Animals & Nature	🦐	shrimp
Animals & Nature	🦑	squid
Animals & Nature	🦪	oyster
Animals & Nature	🐌	snail
Animals & Nature	🦋	butterfly
Animals & Nature	🐛	bug
Animals & Nature	🐜	ant
Animals & Nature	🐝	honeybee
Animals & Nature	🪲	beetle
Animals & Nature	🐞	lady beetle
Animals & Nature	🦗	cricket
Animals & Nature	🪳	cockroach
Animals & Nature	🕷️	spider
Animals & Nature	🕸️	spider web
Animals & Nature	🦂	scorpion
Animals & Nature	🦟	mosquito
Animals & Nature	🪰	fly
Animals & Nature	🪱	worm
Animals & Nature	🦠	microbe
Animals & Nature	💐	bouquet
Animals & Nature	🌸	cherry blossom
Animals & Nature	💮	white flower
Animals & Nature	🪷	lotus
Animals & Nature	🏵️	rosette
Animals & Nature	🌹	rose
Animals & Nature	🥀	wilted flower
Animals & Nature	🌺	hibiscus
Animals & Nature	🌻	sunflower
Animals & Nature	🌼	blossom
Animals & Nature	🌷	tulip
Animals & Nature	🪻	hyacinth
Animals & Nature	🌱	seedling
Animals & Nature	🪴	potted plant
Animals & Nature	🌲	evergreen tree
Animals & Nature	🌳	deciduous tree
Animals & Nature	🌴	palm tree
Animals & Nature	🌵	cactus
Animals & Nature	🌾	sheaf of rice
Animals & Nature	🌿	herb
Animals & Nature	☘️	shamrock
Animals & Nature	🍀	four leaf clover
Animals & Nature	🍁	maple leaf
Animals & Nature	🍂	fallen leaf
Animals & Nature	🍃	leaf fluttering in wind
Animals & Nature	🪹	empty nest
Animals & Nature	🪺	nest with eggs
Animals & Nature	🍄	mushroom
Animals & Nature	🪾	leafless tree
Food & Drink	🍇	grapes
Food & Drink	🍈	melon
Food & Drink	🍉	watermelon
Food & Drink	🍊	tangerine
Food & Drink	🍋	lemon
Food & Drink	🍋‍🟩	lime
Food & Drink	🍌	banana
Food & Drink	🍍	pineapple
Food & Drink	🥭	mango
Food & Drink	🍎	red apple
Food & Drink	🍏	green apple
Food & Drink	🍐	pear
Food & Drink	🍑	peach
Food & Drink	🍒	cherries
Food & Drink	🍓	strawberry
Food & Drink	🫐	blueberries
Food & Drink	🥝	kiwi fruit
Food & Drink	🍅	tomato
Food & Drink	🫒	olive
Food & Drink	🥥	coconut
Food & Drink	🥑	avocado
Food & Drink	🍆	eggplant
Food & Drink	🥔	potato
Food & Drink	🥕	carrot
Food & Drink	🌽	ear of corn
Food & Drink	🌶️	hot pepper
Food & Drink	🫑	bell pepper
Food & Drink	🥒	cucumber
Food & Drink	🥬	leafy green
Food & Drink	🥦	broccoli
Food & Drink	🧄	garlic
Food & Drink	🧅	onion
Food & Drink	🥜	peanuts
Food & Drink	🫘	beans
Food & Drink	🌰	chestnut
Food & Drink	🫚	ginger root
Food & Drink	🫛	pea pod
Food & Drink	🍄‍🟫	brown mushroom
Food & Drink	🫜	root vegetable
Food & Drink	🍞	bread
Food & Drink	🥐	croissant
Food & Drink	🥖	baguette bread
Food & Drink	🫓	flatbread
Food & Drink	🥨	pretzel
Food & Drink	🥯	bagel
Food & Drink	🥞	pancakes
Food & Drink	🧇	waffle
Food & Drink	🧀	cheese wedge
Food & Drink	🍖	meat on bone
Food & Drink	🍗	poultry leg
Food & Drink	🥩	cut of meat
Food & Drink	🥓	bacon
Food & Drink	🍔	hamburger
Food & Drink	🍟	french fries
Food & Drink	🍕	pizza
Food & Drink	🌭	hot dog
Food & Drink	🥪	sandwich
Food & Drink	🌮	taco
Food & Drink	🌯	burrito
Food & Drink	🫔	tamale
Food & Drink	🥙	stuffed flatbread
Food & Drink	🧆	falafel
Food & Drink	🥚	egg
Food & Drink	🍳	cooking
Food & Drink	🥘	shallow pan of food
Food & Drink	🍲	pot of food
Food & Drink	🫕	fondue
Food & Drink	🥣	bowl with spoon
Food & Drink	🥗	green salad
Food & Drink	🍿	popcorn
Food & Drink	🧈	butter
Food & Drink	🧂	salt
Food & Drink	🥫	canned food
Food & Drink	🍱	bento box
Food & Drink	🍘	rice cracker
Food & Drink	🍙	rice ball
Food & Drink	🍚	cooked rice
Food & Drink	🍛	curry rice
Food & Drink	🍜	steaming bowl
Food & Drink	🍝	spaghetti
Food & Drink	🍠	roasted sweet potato
Food & Drink	🍢	oden
Food & Drink	🍣	sushi
Food & Drink	🍤	fried shrimp
Food & Drink	🍥	fish cake with swirl
Food & Drink	🥮	moon cake
Food & Drink	🍡	dango
Food & Drink	🥟	dumpling
Food & Drink	🥠	fortune cookie
Food & Drink	🥡	takeout box
Food & Drink	🍦	soft ice cream
Food & Drink	🍧	shaved ice
Food & Drink	🍨	ice cream
Food & Drink	🍩	doughnut
Food & Drink	🍪	cookie
Food & Drink	🎂	birthday cake
Food & Drink	🍰	shortcake
Food & Drink	🧁	cupcake
Food & Drink	🥧	pie
Food & Drink	🍫	chocolate bar
Food & Drink	🍬	candy
Food & Drink	🍭	lollipop
Food & Drink	🍮	custard
Food & Drink	🍯	honey pot
Food & Drink	🍼	baby bottle
Food & Drink	🥛	glass of milk
Food & Drink	☕	hot beverage
Food & Drink	🫖	teapot
Food & Drink	🍵	teacup without handle
Food & Drink	🍶	sake
Food & Drink	🍾	bottle with popping cork
Food & Drink	🍷	wine glass
Food & Drink	🍸	cocktail glass
Food & Drink	🍹	tropical drink
Food & Drink	🍺	beer mug
Food & Drink	🍻	clinking beer mugs
Food & Drink	🥂	clinking glasses
Food & Drink	🥃	tumbler glass
Food & Drink	🫗	pouring liquid
Food & Drink	🥤	cup with straw
Food & Drink	🧋	bubble tea
Food & Drink	🧃	beverage box
Food & Drink	🧉	mate
Food & Drink	🧊	ice
Food & Drink	🥢	chopsticks
Food & Drink	🍽️	fork and knife with plate
Food & Drink	🍴	fork and knife
Food & Drink	🥄	spoon
Food & Drink	🔪	kitchen knife
Food & Drink	🫙	jar
Food & Drink	🏺	amphora
Travel & Places	🌍	globe showing Europe-Africa
Travel & Places	🌎	globe showing Americas
Travel & Places	🌏	globe showing Asia-Australia
Travel & Places	🌐	globe with meridians
Travel & Places	🗺️	world map
Travel & Places	🗾	map of Japan
Travel & Places	🧭	compass
Travel & Places	🏔️	snow-capped mountain
Travel & Places	⛰️	mountain
Travel & Places	🛘	landslide
Travel & Places	🌋	volcano
Travel & Places	🗻	mount fuji
Travel & Places	🏕️	camping
Travel & Places	🏖️	beach with umbrella
Travel & Places	🏜️	desert
Travel & Places	🏝️	desert island
Travel & Places	🏞️	national park
Travel & Places	🏟️	stadium
Travel & Places	🏛️	classical building
Travel & Places	🏗️	building construction
Travel & Places	🧱	brick
Travel & Places	🪨	rock
Travel & Places	🪵	wood
Travel & Places	🛖	hut
Travel & Places	🏘️	houses
Travel & Places	🏚️	derelict house
Travel & Places	🏠	house
Travel & Places	🏡	house with garden
Travel & Places	🏢	office building
Travel & Places	🏣	Japanese post office
Travel & Places	🏤	post office
Travel & Places	🏥	hospital
Travel & Places	🏦	bank
Travel & Places	🏨	hotel
Travel & Places	🏩	love hotel
Travel & Places	🏪	convenience store
Travel & Places	🏫	school
Travel & Places	🏬	department store
Travel & Places	🏭	factory
Travel & Places	🏯	Japanese castle
Travel & Places	🏰	castle
Travel & Places	💒	wedding
Travel & Places	🗼	Tokyo tower
Travel & Places	🗽	Statue of Liberty
        """,
        """
Travel & Places	⛪	church
Travel & Places	🕌	mosque
Travel & Places	🛕	hindu temple
Travel & Places	🕍	synagogue
Travel & Places	⛩️	shinto shrine
Travel & Places	🕋	kaaba
Travel & Places	⛲	fountain
Travel & Places	⛺	tent
Travel & Places	🌁	foggy
Travel & Places	🌃	night with stars
Travel & Places	🏙️	cityscape
Travel & Places	🌄	sunrise over mountains
Travel & Places	🌅	sunrise
Travel & Places	🌆	cityscape at dusk
Travel & Places	🌇	sunset
Travel & Places	🌉	bridge at night
Travel & Places	♨️	hot springs
Travel & Places	🎠	carousel horse
Travel & Places	🛝	playground slide
Travel & Places	🎡	ferris wheel
Travel & Places	🎢	roller coaster
Travel & Places	💈	barber pole
Travel & Places	🎪	circus tent
Travel & Places	🚂	locomotive
Travel & Places	🚃	railway car
Travel & Places	🚄	high-speed train
Travel & Places	🚅	bullet train
Travel & Places	🚆	train
Travel & Places	🚇	metro
Travel & Places	🚈	light rail
Travel & Places	🚉	station
Travel & Places	🚊	tram
Travel & Places	🚝	monorail
Travel & Places	🚞	mountain railway
Travel & Places	🚋	tram car
Travel & Places	🚌	bus
Travel & Places	🚍	oncoming bus
Travel & Places	🚎	trolleybus
Travel & Places	🚐	minibus
Travel & Places	🚑	ambulance
Travel & Places	🚒	fire engine
Travel & Places	🚓	police car
Travel & Places	🚔	oncoming police car
Travel & Places	🚕	taxi
Travel & Places	🚖	oncoming taxi
Travel & Places	🚗	automobile
Travel & Places	🚘	oncoming automobile
Travel & Places	🚙	sport utility vehicle
Travel & Places	🛻	pickup truck
Travel & Places	🚚	delivery truck
Travel & Places	🚛	articulated lorry
Travel & Places	🚜	tractor
Travel & Places	🏎️	racing car
Travel & Places	🏍️	motorcycle
Travel & Places	🛵	motor scooter
Travel & Places	🦽	manual wheelchair
Travel & Places	🦼	motorized wheelchair
Travel & Places	🛺	auto rickshaw
Travel & Places	🚲	bicycle
Travel & Places	🛴	kick scooter
Travel & Places	🛹	skateboard
Travel & Places	🛼	roller skate
Travel & Places	🚏	bus stop
Travel & Places	🛣️	motorway
Travel & Places	🛤️	railway track
Travel & Places	🛢️	oil drum
Travel & Places	⛽	fuel pump
Travel & Places	🛞	wheel
Travel & Places	🚨	police car light
Travel & Places	🚥	horizontal traffic light
Travel & Places	🚦	vertical traffic light
Travel & Places	🛑	stop sign
Travel & Places	🚧	construction
Travel & Places	⚓	anchor
Travel & Places	🛟	ring buoy
Travel & Places	⛵	sailboat
Travel & Places	🛶	canoe
Travel & Places	🚤	speedboat
Travel & Places	🛳️	passenger ship
Travel & Places	⛴️	ferry
Travel & Places	🛥️	motor boat
Travel & Places	🚢	ship
Travel & Places	✈️	airplane
Travel & Places	🛩️	small airplane
Travel & Places	🛫	airplane departure
Travel & Places	🛬	airplane arrival
Travel & Places	🪂	parachute
Travel & Places	💺	seat
Travel & Places	🚁	helicopter
Travel & Places	🚟	suspension railway
Travel & Places	🚠	mountain cableway
Travel & Places	🚡	aerial tramway
Travel & Places	🛰️	satellite
Travel & Places	🚀	rocket
Travel & Places	🛸	flying saucer
Travel & Places	🛎️	bellhop bell
Travel & Places	🧳	luggage
Travel & Places	⌛	hourglass done
Travel & Places	⏳	hourglass not done
Travel & Places	⌚	watch
Travel & Places	⏰	alarm clock
Travel & Places	⏱️	stopwatch
Travel & Places	⏲️	timer clock
Travel & Places	🕰️	mantelpiece clock
Travel & Places	🕛	twelve o’clock
Travel & Places	🕧	twelve-thirty
Travel & Places	🕐	one o’clock
Travel & Places	🕜	one-thirty
Travel & Places	🕑	two o’clock
Travel & Places	🕝	two-thirty
Travel & Places	🕒	three o’clock
Travel & Places	🕞	three-thirty
Travel & Places	🕓	four o’clock
Travel & Places	🕟	four-thirty
Travel & Places	🕔	five o’clock
Travel & Places	🕠	five-thirty
Travel & Places	🕕	six o’clock
Travel & Places	🕡	six-thirty
Travel & Places	🕖	seven o’clock
Travel & Places	🕢	seven-thirty
Travel & Places	🕗	eight o’clock
Travel & Places	🕣	eight-thirty
Travel & Places	🕘	nine o’clock
Travel & Places	🕤	nine-thirty
Travel & Places	🕙	ten o’clock
Travel & Places	🕥	ten-thirty
Travel & Places	🕚	eleven o’clock
Travel & Places	🕦	eleven-thirty
Travel & Places	🌑	new moon
Travel & Places	🌒	waxing crescent moon
Travel & Places	🌓	first quarter moon
Travel & Places	🌔	waxing gibbous moon
Travel & Places	🌕	full moon
Travel & Places	🌖	waning gibbous moon
Travel & Places	🌗	last quarter moon
Travel & Places	🌘	waning crescent moon
Travel & Places	🌙	crescent moon
Travel & Places	🌚	new moon face
Travel & Places	🌛	first quarter moon face
Travel & Places	🌜	last quarter moon face
Travel & Places	🌡️	thermometer
Travel & Places	☀️	sun
Travel & Places	🌝	full moon face
Travel & Places	🌞	sun with face
Travel & Places	🪐	ringed planet
Travel & Places	⭐	star
Travel & Places	🌟	glowing star
Travel & Places	🌠	shooting star
Travel & Places	🌌	milky way
Travel & Places	☁️	cloud
Travel & Places	⛅	sun behind cloud
Travel & Places	⛈️	cloud with lightning and rain
Travel & Places	🌤️	sun behind small cloud
Travel & Places	🌥️	sun behind large cloud
Travel & Places	🌦️	sun behind rain cloud
Travel & Places	🌧️	cloud with rain
Travel & Places	🌨️	cloud with snow
Travel & Places	🌩️	cloud with lightning
Travel & Places	🌪️	tornado
Travel & Places	🌫️	fog
Travel & Places	🌬️	wind face
Travel & Places	🌀	cyclone
Travel & Places	🌈	rainbow
Travel & Places	🌂	closed umbrella
Travel & Places	☂️	umbrella
Travel & Places	☔	umbrella with rain drops
Travel & Places	⛱️	umbrella on ground
Travel & Places	⚡	high voltage
Travel & Places	❄️	snowflake
Travel & Places	☃️	snowman
Travel & Places	⛄	snowman without snow
Travel & Places	☄️	comet
Travel & Places	🔥	fire
Travel & Places	💧	droplet
Travel & Places	🌊	water wave
Activities	🎃	jack-o-lantern
Activities	🎄	Christmas tree
Activities	🎆	fireworks
Activities	🎇	sparkler
Activities	🧨	firecracker
Activities	✨	sparkles
Activities	🎈	balloon
Activities	🎉	party popper
Activities	🎊	confetti ball
Activities	🎋	tanabata tree
Activities	🎍	pine decoration
Activities	🎎	Japanese dolls
Activities	🎏	carp streamer
Activities	🎐	wind chime
Activities	🎑	moon viewing ceremony
Activities	🧧	red envelope
Activities	🎀	ribbon
Activities	🎁	wrapped gift
Activities	🎗️	reminder ribbon
Activities	🎟️	admission tickets
Activities	🎫	ticket
Activities	🎖️	military medal
Activities	🏆	trophy
Activities	🏅	sports medal
Activities	🥇	1st place medal
Activities	🥈	2nd place medal
Activities	🥉	3rd place medal
Activities	⚽	soccer ball
Activities	⚾	baseball
Activities	🥎	softball
Activities	🏀	basketball
Activities	🏐	volleyball
Activities	🏈	american football
Activities	🏉	rugby football
Activities	🎾	tennis
Activities	🥏	flying disc
Activities	🎳	bowling
Activities	🏏	cricket game
Activities	🏑	field hockey
Activities	🏒	ice hockey
Activities	🥍	lacrosse
Activities	🏓	ping pong
Activities	🏸	badminton
Activities	🥊	boxing glove
Activities	🥋	martial arts uniform
Activities	🥅	goal net
Activities	⛳	flag in hole
Activities	⛸️	ice skate
Activities	🎣	fishing pole
Activities	🤿	diving mask
Activities	🎽	running shirt
Activities	🎿	skis
Activities	🛷	sled
Activities	🥌	curling stone
Activities	🎯	bullseye
Activities	🪀	yo-yo
Activities	🪁	kite
Activities	🔫	water pistol
Activities	🎱	pool 8 ball
Activities	🔮	crystal ball
Activities	🪄	magic wand
Activities	🎮	video game
Activities	🕹️	joystick
Activities	🎰	slot machine
Activities	🎲	game die
Activities	🧩	puzzle piece
Activities	🧸	teddy bear
Activities	🪅	piñata
Activities	🪩	mirror ball
Activities	🪆	nesting dolls
Activities	♠️	spade suit
Activities	♥️	heart suit
Activities	♦️	diamond suit
Activities	♣️	club suit
Activities	♟️	chess pawn
Activities	🃏	joker
Activities	🀄	mahjong red dragon
Activities	🎴	flower playing cards
Activities	🎭	performing arts
Activities	🖼️	framed picture
Activities	🎨	artist palette
Activities	🧵	thread
Activities	🪡	sewing needle
Activities	🧶	yarn
Activities	🪢	knot
Objects	👓	glasses
Objects	🕶️	sunglasses
Objects	🥽	goggles
Objects	🥼	lab coat
Objects	🦺	safety vest
Objects	👔	necktie
Objects	👕	t-shirt
Objects	👖	jeans
Objects	🧣	scarf
Objects	🧤	gloves
Objects	🧥	coat
Objects	🧦	socks
Objects	👗	dress
Objects	👘	kimono
Objects	🥻	sari
Objects	🩱	one-piece swimsuit
Objects	🩲	briefs
Objects	🩳	shorts
Objects	👙	bikini
Objects	👚	woman’s clothes
Objects	🪭	folding hand fan
Objects	👛	purse
Objects	👜	handbag
Objects	👝	clutch bag
Objects	🛍️	shopping bags
Objects	🎒	backpack
Objects	🩴	thong sandal
Objects	👞	man’s shoe
Objects	👟	running shoe
Objects	🥾	hiking boot
Objects	🥿	flat shoe
Objects	👠	high-heeled shoe
Objects	👡	woman’s sandal
Objects	🩰	ballet shoes
Objects	👢	woman’s boot
Objects	🪮	hair pick
Objects	👑	crown
Objects	👒	woman’s hat
Objects	🎩	top hat
Objects	🎓	graduation cap
Objects	🧢	billed cap
Objects	🪖	military helmet
Objects	⛑️	rescue worker’s helmet
Objects	📿	prayer beads
Objects	💄	lipstick
Objects	💍	ring
Objects	💎	gem stone
Objects	🔇	muted speaker
Objects	🔈	speaker low volume
Objects	🔉	speaker medium volume
Objects	🔊	speaker high volume
Objects	📢	loudspeaker
Objects	📣	megaphone
Objects	📯	postal horn
Objects	🔔	bell
Objects	🔕	bell with slash
Objects	🎼	musical score
Objects	🎵	musical note
Objects	🎶	musical notes
Objects	🎙️	studio microphone
Objects	🎚️	level slider
Objects	🎛️	control knobs
Objects	🎤	microphone
Objects	🎧	headphone
Objects	📻	radio
Objects	🎷	saxophone
Objects	🎺	trumpet
Objects	🪊	trombone
Objects	🪗	accordion
Objects	🎸	guitar
Objects	🎹	musical keyboard
Objects	🎻	violin
Objects	🪕	banjo
Objects	🥁	drum
Objects	🪘	long drum
Objects	🪇	maracas
Objects	🪈	flute
Objects	🪉	harp
Objects	📱	mobile phone
Objects	📲	mobile phone with arrow
Objects	☎️	telephone
Objects	📞	telephone receiver
Objects	📟	pager
Objects	📠	fax machine
Objects	🔋	battery
Objects	🪫	low battery
Objects	🔌	electric plug
Objects	💻	laptop
Objects	🖥️	desktop computer
Objects	🖨️	printer
Objects	⌨️	keyboard
Objects	🖱️	computer mouse
Objects	🖲️	trackball
Objects	💽	computer disk
Objects	💾	floppy disk
Objects	💿	optical disk
Objects	📀	dvd
Objects	🧮	abacus
Objects	🎥	movie camera
Objects	🎞️	film frames
Objects	📽️	film projector
Objects	🎬	clapper board
Objects	📺	television
Objects	📷	camera
Objects	📸	camera with flash
Objects	📹	video camera
Objects	📼	videocassette
Objects	🔍	magnifying glass tilted left
Objects	🔎	magnifying glass tilted right
Objects	🕯️	candle
Objects	💡	light bulb
Objects	🔦	flashlight
Objects	🏮	red paper lantern
Objects	🪔	diya lamp
Objects	📔	notebook with decorative cover
Objects	📕	closed book
Objects	📖	open book
Objects	📗	green book
Objects	📘	blue book
Objects	📙	orange book
Objects	📚	books
Objects	📓	notebook
Objects	📒	ledger
Objects	📃	page with curl
Objects	📜	scroll
Objects	📄	page facing up
Objects	📰	newspaper
Objects	🗞️	rolled-up newspaper
Objects	📑	bookmark tabs
Objects	🔖	bookmark
Objects	🏷️	label
Objects	🪙	coin
Objects	💰	money bag
Objects	🪎	treasure chest
Objects	💴	yen banknote
Objects	💵	dollar banknote
Objects	💶	euro banknote
Objects	💷	pound banknote
Objects	💸	money with wings
Objects	💳	credit card
Objects	🧾	receipt
Objects	💹	chart increasing with yen
Objects	✉️	envelope
Objects	📧	e-mail
Objects	📨	incoming envelope
Objects	📩	envelope with arrow
Objects	📤	outbox tray
Objects	📥	inbox tray
Objects	📦	package
Objects	📫	closed mailbox with raised flag
Objects	📪	closed mailbox with lowered flag
Objects	📬	open mailbox with raised flag
Objects	📭	open mailbox with lowered flag
Objects	📮	postbox
Objects	🗳️	ballot box with ballot
Objects	✏️	pencil
Objects	✒️	black nib
Objects	🖋️	fountain pen
Objects	🖊️	pen
Objects	🖌️	paintbrush
Objects	🖍️	crayon
Objects	📝	memo
Objects	💼	briefcase
Objects	📁	file folder
Objects	📂	open file folder
Objects	🗂️	card index dividers
Objects	📅	calendar
Objects	📆	tear-off calendar
Objects	🗒️	spiral notepad
Objects	🗓️	spiral calendar
Objects	📇	card index
Objects	📈	chart increasing
Objects	📉	chart decreasing
Objects	📊	bar chart
Objects	📋	clipboard
Objects	📌	pushpin
Objects	📍	round pushpin
Objects	📎	paperclip
Objects	🖇️	linked paperclips
Objects	📏	straight ruler
Objects	📐	triangular ruler
Objects	✂️	scissors
Objects	🗃️	card file box
Objects	🗄️	file cabinet
Objects	🗑️	wastebasket
Objects	🔒	locked
Objects	🔓	unlocked
Objects	🔏	locked with pen
Objects	🔐	locked with key
Objects	🔑	key
Objects	🗝️	old key
Objects	🔨	hammer
Objects	🪓	axe
Objects	⛏️	pick
Objects	⚒️	hammer and pick
Objects	🛠️	hammer and wrench
Objects	🗡️	dagger
Objects	⚔️	crossed swords
Objects	💣	bomb
Objects	🪃	boomerang
Objects	🏹	bow and arrow
Objects	🛡️	shield
Objects	🪚	carpentry saw
Objects	🔧	wrench
Objects	🪛	screwdriver
Objects	🔩	nut and bolt
Objects	⚙️	gear
Objects	🗜️	clamp
Objects	⚖️	balance scale
Objects	🦯	white cane
Objects	🔗	link
Objects	⛓️‍💥	broken chain
Objects	⛓️	chains
Objects	🪝	hook
Objects	🧰	toolbox
Objects	🧲	magnet
Objects	🪜	ladder
Objects	🪏	shovel
Objects	⚗️	alembic
Objects	🧪	test tube
Objects	🧫	petri dish
Objects	🧬	dna
Objects	🔬	microscope
Objects	🔭	telescope
Objects	📡	satellite antenna
Objects	💉	syringe
Objects	🩸	drop of blood
Objects	💊	pill
Objects	🩹	adhesive bandage
Objects	🩼	crutch
Objects	🩺	stethoscope
Objects	🩻	x-ray
Objects	🚪	door
Objects	🛗	elevator
Objects	🪞	mirror
Objects	🪟	window
Objects	🛏️	bed
Objects	🛋️	couch and lamp
Objects	🪑	chair
Objects	🚽	toilet
Objects	🪠	plunger
Objects	🚿	shower
Objects	🛁	bathtub
Objects	🪤	mouse trap
Objects	🪒	razor
Objects	🧴	lotion bottle
Objects	🧷	safety pin
Objects	🧹	broom
Objects	🧺	basket
Objects	🧻	roll of paper
Objects	🪣	bucket
Objects	🧼	soap
Objects	🫧	bubbles
Objects	🪥	toothbrush
Objects	🧽	sponge
Objects	🧯	fire extinguisher
Objects	🛒	shopping cart
Objects	🚬	cigarette
Objects	⚰️	coffin
Objects	🪦	headstone
Objects	⚱️	funeral urn
Objects	🧿	nazar amulet
Objects	🪬	hamsa
Objects	🗿	moai
Objects	🪧	placard
Objects	🪪	identification card
Symbols	🏧	ATM sign
Symbols	🚮	litter in bin sign
Symbols	🚰	potable water
Symbols	♿	wheelchair symbol
Symbols	🚹	men’s room
Symbols	🚺	women’s room
Symbols	🚻	restroom
Symbols	🚼	baby symbol
Symbols	🚾	water closet
Symbols	🛂	passport control
Symbols	🛃	customs
Symbols	🛄	baggage claim
Symbols	🛅	left luggage
Symbols	⚠️	warning
Symbols	🚸	children crossing
Symbols	⛔	no entry
Symbols	🚫	prohibited
Symbols	🚳	no bicycles
Symbols	🚭	no smoking
Symbols	🚯	no littering
Symbols	🚱	non-potable water
Symbols	🚷	no pedestrians
Symbols	📵	no mobile phones
Symbols	🔞	no one under eighteen
Symbols	☢️	radioactive
Symbols	☣️	biohazard
Symbols	⬆️	up arrow
Symbols	↗️	up-right arrow
Symbols	➡️	right arrow
Symbols	↘️	down-right arrow
Symbols	⬇️	down arrow
Symbols	↙️	down-left arrow
Symbols	⬅️	left arrow
Symbols	↖️	up-left arrow
Symbols	↕️	up-down arrow
Symbols	↔️	left-right arrow
Symbols	↩️	right arrow curving left
Symbols	↪️	left arrow curving right
Symbols	⤴️	right arrow curving up
Symbols	⤵️	right arrow curving down
Symbols	🔃	clockwise vertical arrows
Symbols	🔄	counterclockwise arrows button
Symbols	🔙	BACK arrow
Symbols	🔚	END arrow
Symbols	🔛	ON! arrow
Symbols	🔜	SOON arrow
Symbols	🔝	TOP arrow
Symbols	🛐	place of worship
Symbols	⚛️	atom symbol
Symbols	🕉️	om
Symbols	✡️	star of David
Symbols	☸️	wheel of dharma
Symbols	☯️	yin yang
Symbols	✝️	latin cross
Symbols	☦️	orthodox cross
Symbols	☪️	star and crescent
Symbols	☮️	peace symbol
Symbols	🕎	menorah
Symbols	🔯	dotted six-pointed star
Symbols	🪯	khanda
Symbols	♈	Aries
Symbols	♉	Taurus
Symbols	♊	Gemini
Symbols	♋	Cancer
Symbols	♌	Leo
Symbols	♍	Virgo
Symbols	♎	Libra
Symbols	♏	Scorpio
Symbols	♐	Sagittarius
Symbols	♑	Capricorn
Symbols	♒	Aquarius
Symbols	♓	Pisces
Symbols	⛎	Ophiuchus
Symbols	🔀	shuffle tracks button
Symbols	🔁	repeat button
Symbols	🔂	repeat single button
Symbols	▶️	play button
Symbols	⏩	fast-forward button
Symbols	⏭️	next track button
Symbols	⏯️	play or pause button
Symbols	◀️	reverse button
Symbols	⏪	fast reverse button
Symbols	⏮️	last track button
Symbols	🔼	upwards button
Symbols	⏫	fast up button
Symbols	🔽	downwards button
Symbols	⏬	fast down button
Symbols	⏸️	pause button
Symbols	⏹️	stop button
Symbols	⏺️	record button
Symbols	⏏️	eject button
Symbols	🎦	cinema
Symbols	🔅	dim button
Symbols	🔆	bright button
Symbols	📶	antenna bars
Symbols	🛜	wireless
Symbols	📳	vibration mode
Symbols	📴	mobile phone off
Symbols	♀️	female sign
Symbols	♂️	male sign
Symbols	⚧️	transgender symbol
Symbols	✖️	multiply
Symbols	➕	plus
Symbols	➖	minus
Symbols	➗	divide
Symbols	🟰	heavy equals sign
Symbols	♾️	infinity
Symbols	‼️	double exclamation mark
        """,
        """
Symbols	⁉️	exclamation question mark
Symbols	❓	red question mark
Symbols	❔	white question mark
Symbols	❕	white exclamation mark
Symbols	❗	red exclamation mark
Symbols	〰️	wavy dash
Symbols	💱	currency exchange
Symbols	💲	heavy dollar sign
Symbols	⚕️	medical symbol
Symbols	♻️	recycling symbol
Symbols	⚜️	fleur-de-lis
Symbols	🔱	trident emblem
Symbols	📛	name badge
Symbols	🔰	Japanese symbol for beginner
Symbols	⭕	hollow red circle
Symbols	✅	check mark button
Symbols	☑️	check box with check
Symbols	✔️	check mark
Symbols	❌	cross mark
Symbols	❎	cross mark button
Symbols	➰	curly loop
Symbols	➿	double curly loop
Symbols	〽️	part alternation mark
Symbols	✳️	eight-spoked asterisk
Symbols	✴️	eight-pointed star
Symbols	❇️	sparkle
Symbols	©️	copyright
Symbols	®️	registered
Symbols	™️	trade mark
Symbols	🫟	splatter
Symbols	#️⃣	keycap: #
Symbols	*️⃣	keycap: *
Symbols	0️⃣	keycap: 0
Symbols	1️⃣	keycap: 1
Symbols	2️⃣	keycap: 2
Symbols	3️⃣	keycap: 3
Symbols	4️⃣	keycap: 4
Symbols	5️⃣	keycap: 5
Symbols	6️⃣	keycap: 6
Symbols	7️⃣	keycap: 7
Symbols	8️⃣	keycap: 8
Symbols	9️⃣	keycap: 9
Symbols	🔟	keycap: 10
Symbols	🔠	input latin uppercase
Symbols	🔡	input latin lowercase
Symbols	🔢	input numbers
Symbols	🔣	input symbols
Symbols	🔤	input latin letters
Symbols	🅰️	A button (blood type)
Symbols	🆎	AB button (blood type)
Symbols	🅱️	B button (blood type)
Symbols	🆑	CL button
Symbols	🆒	COOL button
Symbols	🆓	FREE button
Symbols	ℹ️	information
Symbols	🆔	ID button
Symbols	Ⓜ️	circled M
Symbols	🆕	NEW button
Symbols	🆖	NG button
Symbols	🅾️	O button (blood type)
Symbols	🆗	OK button
Symbols	🅿️	P button
Symbols	🆘	SOS button
Symbols	🆙	UP! button
Symbols	🆚	VS button
Symbols	🈁	Japanese “here” button
Symbols	🈂️	Japanese “service charge” button
Symbols	🈷️	Japanese “monthly amount” button
Symbols	🈶	Japanese “not free of charge” button
Symbols	🈯	Japanese “reserved” button
Symbols	🉐	Japanese “bargain” button
Symbols	🈹	Japanese “discount” button
Symbols	🈚	Japanese “free of charge” button
Symbols	🈲	Japanese “prohibited” button
Symbols	🉑	Japanese “acceptable” button
Symbols	🈸	Japanese “application” button
Symbols	🈴	Japanese “passing grade” button
Symbols	🈳	Japanese “vacancy” button
Symbols	㊗️	Japanese “congratulations” button
Symbols	㊙️	Japanese “secret” button
Symbols	🈺	Japanese “open for business” button
Symbols	🈵	Japanese “no vacancy” button
Symbols	🔴	red circle
Symbols	🟠	orange circle
Symbols	🟡	yellow circle
Symbols	🟢	green circle
Symbols	🔵	blue circle
Symbols	🟣	purple circle
Symbols	🟤	brown circle
Symbols	⚫	black circle
Symbols	⚪	white circle
Symbols	🟥	red square
Symbols	🟧	orange square
Symbols	🟨	yellow square
Symbols	🟩	green square
Symbols	🟦	blue square
Symbols	🟪	purple square
Symbols	🟫	brown square
Symbols	⬛	black large square
Symbols	⬜	white large square
Symbols	◼️	black medium square
Symbols	◻️	white medium square
Symbols	◾	black medium-small square
Symbols	◽	white medium-small square
Symbols	▪️	black small square
Symbols	▫️	white small square
Symbols	🔶	large orange diamond
Symbols	🔷	large blue diamond
Symbols	🔸	small orange diamond
Symbols	🔹	small blue diamond
Symbols	🔺	red triangle pointed up
Symbols	🔻	red triangle pointed down
Symbols	💠	diamond with a dot
Symbols	🔘	radio button
Symbols	🔳	white square button
Symbols	🔲	black square button
Flags	🏁	chequered flag
Flags	🚩	triangular flag
Flags	🎌	crossed flags
Flags	🏴	black flag
Flags	🏳️	white flag
Flags	🏳️‍🌈	rainbow flag
Flags	🏳️‍⚧️	transgender flag
Flags	🏴‍☠️	pirate flag
Flags	🇦🇨	flag: Ascension Island
Flags	🇦🇩	flag: Andorra
Flags	🇦🇪	flag: United Arab Emirates
Flags	🇦🇫	flag: Afghanistan
Flags	🇦🇬	flag: Antigua & Barbuda
Flags	🇦🇮	flag: Anguilla
Flags	🇦🇱	flag: Albania
Flags	🇦🇲	flag: Armenia
Flags	🇦🇴	flag: Angola
Flags	🇦🇶	flag: Antarctica
Flags	🇦🇷	flag: Argentina
Flags	🇦🇸	flag: American Samoa
Flags	🇦🇹	flag: Austria
Flags	🇦🇺	flag: Australia
Flags	🇦🇼	flag: Aruba
Flags	🇦🇽	flag: Åland Islands
Flags	🇦🇿	flag: Azerbaijan
Flags	🇧🇦	flag: Bosnia & Herzegovina
Flags	🇧🇧	flag: Barbados
Flags	🇧🇩	flag: Bangladesh
Flags	🇧🇪	flag: Belgium
Flags	🇧🇫	flag: Burkina Faso
Flags	🇧🇬	flag: Bulgaria
Flags	🇧🇭	flag: Bahrain
Flags	🇧🇮	flag: Burundi
Flags	🇧🇯	flag: Benin
Flags	🇧🇱	flag: St. Barthélemy
Flags	🇧🇲	flag: Bermuda
Flags	🇧🇳	flag: Brunei
Flags	🇧🇴	flag: Bolivia
Flags	🇧🇶	flag: Caribbean Netherlands
Flags	🇧🇷	flag: Brazil
Flags	🇧🇸	flag: Bahamas
Flags	🇧🇹	flag: Bhutan
Flags	🇧🇻	flag: Bouvet Island
Flags	🇧🇼	flag: Botswana
Flags	🇧🇾	flag: Belarus
Flags	🇧🇿	flag: Belize
Flags	🇨🇦	flag: Canada
Flags	🇨🇨	flag: Cocos (Keeling) Islands
Flags	🇨🇩	flag: Congo - Kinshasa
Flags	🇨🇫	flag: Central African Republic
Flags	🇨🇬	flag: Congo - Brazzaville
Flags	🇨🇭	flag: Switzerland
Flags	🇨🇮	flag: Côte d’Ivoire
Flags	🇨🇰	flag: Cook Islands
Flags	🇨🇱	flag: Chile
Flags	🇨🇲	flag: Cameroon
Flags	🇨🇳	flag: China
Flags	🇨🇴	flag: Colombia
Flags	🇨🇵	flag: Clipperton Island
Flags	🇨🇶	flag: Sark
Flags	🇨🇷	flag: Costa Rica
Flags	🇨🇺	flag: Cuba
Flags	🇨🇻	flag: Cape Verde
Flags	🇨🇼	flag: Curaçao
Flags	🇨🇽	flag: Christmas Island
Flags	🇨🇾	flag: Cyprus
Flags	🇨🇿	flag: Czechia
Flags	🇩🇪	flag: Germany
Flags	🇩🇬	flag: Diego Garcia
Flags	🇩🇯	flag: Djibouti
Flags	🇩🇰	flag: Denmark
Flags	🇩🇲	flag: Dominica
Flags	🇩🇴	flag: Dominican Republic
Flags	🇩🇿	flag: Algeria
Flags	🇪🇦	flag: Ceuta & Melilla
Flags	🇪🇨	flag: Ecuador
Flags	🇪🇪	flag: Estonia
Flags	🇪🇬	flag: Egypt
Flags	🇪🇭	flag: Western Sahara
Flags	🇪🇷	flag: Eritrea
Flags	🇪🇸	flag: Spain
Flags	🇪🇹	flag: Ethiopia
Flags	🇪🇺	flag: European Union
Flags	🇫🇮	flag: Finland
Flags	🇫🇯	flag: Fiji
Flags	🇫🇰	flag: Falkland Islands
Flags	🇫🇲	flag: Micronesia
Flags	🇫🇴	flag: Faroe Islands
Flags	🇫🇷	flag: France
Flags	🇬🇦	flag: Gabon
Flags	🇬🇧	flag: United Kingdom
Flags	🇬🇩	flag: Grenada
Flags	🇬🇪	flag: Georgia
Flags	🇬🇫	flag: French Guiana
Flags	🇬🇬	flag: Guernsey
Flags	🇬🇭	flag: Ghana
Flags	🇬🇮	flag: Gibraltar
Flags	🇬🇱	flag: Greenland
Flags	🇬🇲	flag: Gambia
Flags	🇬🇳	flag: Guinea
Flags	🇬🇵	flag: Guadeloupe
Flags	🇬🇶	flag: Equatorial Guinea
Flags	🇬🇷	flag: Greece
Flags	🇬🇸	flag: South Georgia & South Sandwich Islands
Flags	🇬🇹	flag: Guatemala
Flags	🇬🇺	flag: Guam
Flags	🇬🇼	flag: Guinea-Bissau
Flags	🇬🇾	flag: Guyana
Flags	🇭🇰	flag: Hong Kong SAR China
Flags	🇭🇲	flag: Heard & McDonald Islands
Flags	🇭🇳	flag: Honduras
Flags	🇭🇷	flag: Croatia
Flags	🇭🇹	flag: Haiti
Flags	🇭🇺	flag: Hungary
Flags	🇮🇨	flag: Canary Islands
Flags	🇮🇩	flag: Indonesia
Flags	🇮🇪	flag: Ireland
Flags	🇮🇱	flag: Israel
Flags	🇮🇲	flag: Isle of Man
Flags	🇮🇳	flag: India
Flags	🇮🇴	flag: British Indian Ocean Territory
Flags	🇮🇶	flag: Iraq
Flags	🇮🇷	flag: Iran
Flags	🇮🇸	flag: Iceland
Flags	🇮🇹	flag: Italy
Flags	🇯🇪	flag: Jersey
Flags	🇯🇲	flag: Jamaica
Flags	🇯🇴	flag: Jordan
Flags	🇯🇵	flag: Japan
Flags	🇰🇪	flag: Kenya
Flags	🇰🇬	flag: Kyrgyzstan
Flags	🇰🇭	flag: Cambodia
Flags	🇰🇮	flag: Kiribati
Flags	🇰🇲	flag: Comoros
Flags	🇰🇳	flag: St. Kitts & Nevis
Flags	🇰🇵	flag: North Korea
Flags	🇰🇷	flag: South Korea
Flags	🇰🇼	flag: Kuwait
Flags	🇰🇾	flag: Cayman Islands
Flags	🇰🇿	flag: Kazakhstan
Flags	🇱🇦	flag: Laos
Flags	🇱🇧	flag: Lebanon
Flags	🇱🇨	flag: St. Lucia
Flags	🇱🇮	flag: Liechtenstein
Flags	🇱🇰	flag: Sri Lanka
Flags	🇱🇷	flag: Liberia
Flags	🇱🇸	flag: Lesotho
Flags	🇱🇹	flag: Lithuania
Flags	🇱🇺	flag: Luxembourg
Flags	🇱🇻	flag: Latvia
Flags	🇱🇾	flag: Libya
Flags	🇲🇦	flag: Morocco
Flags	🇲🇨	flag: Monaco
Flags	🇲🇩	flag: Moldova
Flags	🇲🇪	flag: Montenegro
Flags	🇲🇫	flag: St. Martin
Flags	🇲🇬	flag: Madagascar
Flags	🇲🇭	flag: Marshall Islands
Flags	🇲🇰	flag: North Macedonia
Flags	🇲🇱	flag: Mali
Flags	🇲🇲	flag: Myanmar (Burma)
Flags	🇲🇳	flag: Mongolia
Flags	🇲🇴	flag: Macao SAR China
Flags	🇲🇵	flag: Northern Mariana Islands
Flags	🇲🇶	flag: Martinique
Flags	🇲🇷	flag: Mauritania
Flags	🇲🇸	flag: Montserrat
Flags	🇲🇹	flag: Malta
Flags	🇲🇺	flag: Mauritius
Flags	🇲🇻	flag: Maldives
Flags	🇲🇼	flag: Malawi
Flags	🇲🇽	flag: Mexico
Flags	🇲🇾	flag: Malaysia
Flags	🇲🇿	flag: Mozambique
Flags	🇳🇦	flag: Namibia
Flags	🇳🇨	flag: New Caledonia
Flags	🇳🇪	flag: Niger
Flags	🇳🇫	flag: Norfolk Island
Flags	🇳🇬	flag: Nigeria
Flags	🇳🇮	flag: Nicaragua
Flags	🇳🇱	flag: Netherlands
Flags	🇳🇴	flag: Norway
Flags	🇳🇵	flag: Nepal
Flags	🇳🇷	flag: Nauru
Flags	🇳🇺	flag: Niue
Flags	🇳🇿	flag: New Zealand
Flags	🇴🇲	flag: Oman
Flags	🇵🇦	flag: Panama
Flags	🇵🇪	flag: Peru
Flags	🇵🇫	flag: French Polynesia
Flags	🇵🇬	flag: Papua New Guinea
Flags	🇵🇭	flag: Philippines
Flags	🇵🇰	flag: Pakistan
Flags	🇵🇱	flag: Poland
Flags	🇵🇲	flag: St. Pierre & Miquelon
Flags	🇵🇳	flag: Pitcairn Islands
Flags	🇵🇷	flag: Puerto Rico
Flags	🇵🇸	flag: Palestinian Territories
Flags	🇵🇹	flag: Portugal
Flags	🇵🇼	flag: Palau
Flags	🇵🇾	flag: Paraguay
Flags	🇶🇦	flag: Qatar
Flags	🇷🇪	flag: Réunion
Flags	🇷🇴	flag: Romania
Flags	🇷🇸	flag: Serbia
Flags	🇷🇺	flag: Russia
Flags	🇷🇼	flag: Rwanda
Flags	🇸🇦	flag: Saudi Arabia
Flags	🇸🇧	flag: Solomon Islands
Flags	🇸🇨	flag: Seychelles
Flags	🇸🇩	flag: Sudan
Flags	🇸🇪	flag: Sweden
Flags	🇸🇬	flag: Singapore
Flags	🇸🇭	flag: St. Helena
Flags	🇸🇮	flag: Slovenia
Flags	🇸🇯	flag: Svalbard & Jan Mayen
Flags	🇸🇰	flag: Slovakia
Flags	🇸🇱	flag: Sierra Leone
Flags	🇸🇲	flag: San Marino
Flags	🇸🇳	flag: Senegal
Flags	🇸🇴	flag: Somalia
Flags	🇸🇷	flag: Suriname
Flags	🇸🇸	flag: South Sudan
Flags	🇸🇹	flag: São Tomé & Príncipe
Flags	🇸🇻	flag: El Salvador
Flags	🇸🇽	flag: Sint Maarten
Flags	🇸🇾	flag: Syria
Flags	🇸🇿	flag: Eswatini
Flags	🇹🇦	flag: Tristan da Cunha
Flags	🇹🇨	flag: Turks & Caicos Islands
Flags	🇹🇩	flag: Chad
Flags	🇹🇫	flag: French Southern Territories
Flags	🇹🇬	flag: Togo
Flags	🇹🇭	flag: Thailand
Flags	🇹🇯	flag: Tajikistan
Flags	🇹🇰	flag: Tokelau
Flags	🇹🇱	flag: Timor-Leste
Flags	🇹🇲	flag: Turkmenistan
Flags	🇹🇳	flag: Tunisia
Flags	🇹🇴	flag: Tonga
Flags	🇹🇷	flag: Türkiye
Flags	🇹🇹	flag: Trinidad & Tobago
Flags	🇹🇻	flag: Tuvalu
Flags	🇹🇼	flag: Taiwan
Flags	🇹🇿	flag: Tanzania
Flags	🇺🇦	flag: Ukraine
Flags	🇺🇬	flag: Uganda
Flags	🇺🇲	flag: U.S. Outlying Islands
Flags	🇺🇳	flag: United Nations
Flags	🇺🇸	flag: United States
Flags	🇺🇾	flag: Uruguay
Flags	🇺🇿	flag: Uzbekistan
Flags	🇻🇦	flag: Vatican City
Flags	🇻🇨	flag: St. Vincent & Grenadines
Flags	🇻🇪	flag: Venezuela
Flags	🇻🇬	flag: British Virgin Islands
Flags	🇻🇮	flag: U.S. Virgin Islands
Flags	🇻🇳	flag: Vietnam
Flags	🇻🇺	flag: Vanuatu
Flags	🇼🇫	flag: Wallis & Futuna
Flags	🇼🇸	flag: Samoa
Flags	🇽🇰	flag: Kosovo
Flags	🇾🇪	flag: Yemen
Flags	🇾🇹	flag: Mayotte
Flags	🇿🇦	flag: South Africa
Flags	🇿🇲	flag: Zambia
Flags	🇿🇼	flag: Zimbabwe
Flags	🏴󠁧󠁢󠁥󠁮󠁧󠁿	flag: England
Flags	🏴󠁧󠁢󠁳󠁣󠁴󠁿	flag: Scotland
Flags	🏴󠁧󠁢󠁷󠁬󠁳󠁿	flag: Wales
        """
    )
}
