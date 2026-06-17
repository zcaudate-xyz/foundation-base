(ns gdscript.core.builtin
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :gdscript
  gdscript.core
  {:macro-only true})

;; 114 global utility functions
(def +utility-functions+
  '[sin cos tan sinh cosh tanh asin acos atan atan2 asinh acosh atanh sqrt fmod
    fposmod posmod floor floorf floori ceil ceilf ceili round roundf roundi abs
    absf absi sign signf signi snapped snappedf snappedi pow log exp is-nan
    is-inf is-equal-approx is-zero-approx is-finite ease step-decimals lerp
    lerpf cubic-interpolate cubic-interpolate-angle cubic-interpolate-in-time
    cubic-interpolate-angle-in-time bezier-interpolate bezier-derivative
    angle-difference lerp-angle inverse-lerp remap smoothstep move-toward
    rotate-toward deg-to-rad rad-to-deg linear-to-db db-to-linear wrap wrapi
    wrapf max maxi maxf min mini minf clamp clampi clampf nearest-po2 pingpong
    randomize randi randf randi-range randf-range randfn seed rand-from-seed
    weakref typeof type-convert str error-string type-string print print-rich
    printerr printt prints printraw print-verbose push-error push-warning
    var-to-str str-to-var var-to-bytes bytes-to-var var-to-bytes-with-objects
    bytes-to-var-with-objects hash instance-from-id is-instance-id-valid
    is-instance-valid rid-allocate-id rid-from-int64 is-same])

;; 38 built-in value types
(def +builtin-classes+
  '[Nil bool int float String Vector2 Vector2i Rect2 Rect2i Vector3 Vector3i
    Transform2D Vector4 Vector4i Plane Quaternion AABB Basis Transform3D
    Projection Color StringName NodePath RID Callable Signal Dictionary Array
    PackedByteArray PackedInt32Array PackedInt64Array PackedFloat32Array
    PackedFloat64Array PackedStringArray PackedVector2Array PackedVector3Array
    PackedColorArray PackedVector4Array])

;; 39 engine singletons
(def +singletons+
  '[AudioServer CameraServer ClassDB DisplayServer EditorInterface Engine
    EngineDebugger Geometry2D Geometry3D GDExtensionManager IP Input InputMap
    JavaClassWrapper JavaScriptBridge Marshalls NativeMenu NavigationMeshGenerator
    NavigationServer2D NavigationServer2DManager NavigationServer3D
    NavigationServer3DManager OS Performance PhysicsServer2D
    PhysicsServer2DManager PhysicsServer3D PhysicsServer3DManager ProjectSettings
    RenderingServer ResourceLoader ResourceSaver ResourceUID TextServerManager
    ThemeDB Time TranslationServer WorkerThreadPool XRServer])

;; 1024 engine classes grouped by their nearest non-Object ancestor
(def +classes-node+
  '[AcceptDialog AimModifier3D AnimatableBody2D AnimatableBody3D
    AnimatedSprite2D AnimatedSprite3D AnimationMixer AnimationPlayer
    AnimationTree Area2D Area3D AspectRatioContainer AudioListener2D
    AudioListener3D AudioStreamPlayer AudioStreamPlayer2D AudioStreamPlayer3D
    BackBufferCopy BaseButton Bone2D BoneAttachment3D BoneConstraint3D
    BoneTwistDisperser3D BoxContainer Button CCDIK3D CPUParticles2D
    CPUParticles3D CSGBox3D CSGCombiner3D CSGCylinder3D CSGMesh3D
    CSGPolygon3D CSGPrimitive3D CSGShape3D CSGSphere3D CSGTorus3D Camera2D
    Camera3D CanvasGroup CanvasItem CanvasLayer CanvasModulate CenterContainer
    ChainIK3D CharacterBody2D CharacterBody3D CheckBox CheckButton CodeEdit
    CollisionObject2D CollisionObject3D CollisionPolygon2D CollisionPolygon3D
    CollisionShape2D CollisionShape3D ColorPicker ColorPickerButton ColorRect
    ConeTwistJoint3D ConfirmationDialog Container Control
    ConvertTransformModifier3D CopyTransformModifier3D DampedSpringJoint2D
    Decal DirectionalLight2D DirectionalLight3D EditorCommandPalette EditorDock
    EditorFileDialog EditorFileSystem EditorInspector EditorPlugin EditorProperty
    EditorResourcePicker EditorResourcePreview EditorScriptPicker
    EditorSpinSlider EditorToaster FABRIK3D FileDialog FileSystemDock
    FlowContainer FogVolume FoldableContainer GPUParticles2D GPUParticles3D
    GPUParticlesAttractor3D GPUParticlesAttractorBox3D
    GPUParticlesAttractorSphere3D GPUParticlesAttractorVectorField3D
    GPUParticlesCollision3D GPUParticlesCollisionBox3D
    GPUParticlesCollisionHeightField3D GPUParticlesCollisionSDF3D
    GPUParticlesCollisionSphere3D Generic6DOFJoint3D GeometryInstance3D GraphEdit
    GraphElement GraphFrame GraphNode GridContainer GridMap GridMapEditorPlugin
    HBoxContainer HFlowContainer HSeparator HSlider HSplitContainer
    ImporterMeshInstance3D Joint2D Joint3D Label Label3D Light2D Light3D
    Line2D LineEdit MarginContainer Marker2D Marker3D MenuBar MenuButton
    MeshInstance2D MeshInstance3D MissingNode MotorJoint2D NavigationAgent2D
    NavigationAgent3D NavigationLink2D NavigationLink3D NavigationObstacle2D
    NavigationObstacle3D NavigationRegion2D NavigationRegion3D NinePatchRect
    Node Node2D Node3D OccluderInstance3D OmniLight3D OpenXRCompositionLayer
    OpenXRCompositionLayerCylinder OpenXRCompositionLayerEquirect
    OpenXRCompositionLayerQuad OpenXRFaceTracker OpenXRHand OpenXRIPBinding
    OpenXRInteractionProfile OpenXRInteractionProfileMetadata OpenXRPose
    OpenXRPoseAdvanced OpenXRPoseWithFallback OpenXRSpatialAnchor
    OpenXRSpatialEntity OpenXRSpatialEntityComponentExtension
    OpenXRSpatialEntityReferenceFrame OpenXRSpatialMarker
    OpenXRSpatialPlaneContainer OpenXRSpatialShape OpenXRSpatialTriangleStrip
    OpenXRTrackedHand OpenXRVirtualKeyboard OpenXRViewportCompositionLayer
    Panel PanelContainer ParallaxBackground ParallaxLayer Path2D Path3D
    PathFollow2D PathFollow3D PhysicalBone2D PhysicalBone3D
    PhysicalBoneSimulator3D PhysicsBody2D PhysicsBody3D PinJoint2D PinJoint3D
    PointLight2D Polygon2D Popup PopupMenu PopupPanel ProgressBar
    Range ReferenceRect RemoteTransform2D RemoteTransform3D ResourcePreloader
    RichTextLabel RigidBody2D RigidBody3D RootMotionView ScrollContainer
    Separator ShapeCast2D ShapeCast3D Skeleton2D Skeleton3D SkeletonIK3D
    SoftBody3D SpinBox SplitContainer SpringArm3D Sprite2D Sprite3D
    StaticBody2D StaticBody3D StatusIndicator SubViewport TabBar TabContainer
    TextEdit TextureButton TextureProgressBar TextureRect TileMap TileMapLayer
    TouchScreenButton Tree TreeItem type VBoxContainer VehicleBody3D
    VehicleWheel3D VideoStreamPlayer Viewport VFlowContainer
    VisibleOnScreenEnabler2D VisibleOnScreenEnabler3D VisibleOnScreenNotifier2D
    VisibleOnScreenNotifier3D VSeparator VSlider VSplitContainer
    WebRTCMultiplayerPeer WebRTCPeerConnection Window World2D World3D
    XRAnchor3D XRCamera3D XRController3D XRNode3D XROrigin3D])

(def +classes-node2d+
  '[AnimatableBody2D AnimatedSprite2D Area2D AudioListener2D AudioStreamPlayer2D
    BackBufferCopy Bone2D Camera2D CanvasGroup CanvasItem CanvasLayer
    CanvasModulate CharacterBody2D CollisionObject2D CollisionPolygon2D
    CollisionShape2D CPUParticles2D DampedSpringJoint2D DirectionalLight2D
    Joint2D Light2D Line2D Marker2D MeshInstance2D MotorJoint2D NavigationAgent2D
    NavigationLink2D NavigationObstacle2D NavigationRegion2D Node2D
    ParallaxLayer Path2D PathFollow2D PhysicalBone2D PhysicsBody2D PinJoint2D
    PointLight2D Polygon2D RemoteTransform2D RigidBody2D ShapeCast2D Skeleton2D
    Sprite2D StaticBody2D TileMap TileMapLayer TouchScreenButton
    VisibleOnScreenEnabler2D VisibleOnScreenNotifier2D World2D])

(def +classes-node3d+
  '[AimModifier3D AnimatableBody3D AnimatedSprite3D Area3D AudioListener3D
    AudioStreamPlayer3D BoneAttachment3D BoneConstraint3D BoneTwistDisperser3D
    Camera3D CSGBox3D CSGCombiner3D CSGCylinder3D CSGMesh3D CSGPolygon3D
    CSGPrimitive3D CSGShape3D CSGSphere3D CSGTorus3D Decal DirectionalLight3D
    FogVolume GeometryInstance3D GPUParticles3D GPUParticlesAttractor3D
    GPUParticlesAttractorBox3D GPUParticlesAttractorSphere3D
    GPUParticlesAttractorVectorField3D GPUParticlesCollision3D
    GPUParticlesCollisionBox3D GPUParticlesCollisionHeightField3D
    GPUParticlesCollisionSDF3D GPUParticlesCollisionSphere3D Generic6DOFJoint3D
    GridMap ImporterMeshInstance3D Joint3D Light3D Marker3D MeshInstance3D
    NavigationAgent3D NavigationLink3D NavigationObstacle3D NavigationRegion3D
    Node3D OccluderInstance3D OmniLight3D PhysicalBone3D
    PhysicalBoneSimulator3D PhysicsBody3D PinJoint3D RemoteTransform3D
    RigidBody3D ShapeCast3D Skeleton3D SkeletonIK3D SoftBody3D SpringArm3D
    Sprite3D StaticBody3D VehicleBody3D VehicleWheel3D VisibleOnScreenEnabler3D
    VisibleOnScreenNotifier3D World3D XRAnchor3D XRCamera3D XRController3D
    XRNode3D XROrigin3D])

(def +classes-control+
  '[AspectRatioContainer BaseButton BoxContainer Button CenterContainer CheckBox
    CheckButton CodeEdit ColorPicker ColorPickerButton ColorRect ConfirmationDialog
    Container Control EditorCommandPalette EditorDock EditorFileDialog
    EditorInspector EditorPlugin EditorProperty EditorResourcePicker
    EditorResourcePreview EditorScriptPicker EditorSpinSlider EditorToaster
    FileDialog FileSystemDock FlowContainer FoldableContainer GraphEdit
    GraphElement GraphFrame GraphNode GridContainer HBoxContainer HFlowContainer
    HSeparator HSlider HSplitContainer ItemList Label LineEdit MarginContainer
    MenuBar MenuButton NinePatchRect Panel PanelContainer Popup PopupMenu
    PopupPanel ProgressBar Range ReferenceRect RichTextLabel ScrollContainer
    Separator SpinBox SplitContainer TabBar TabContainer TextEdit TextureButton
    TextureProgressBar TextureRect Tree VBoxContainer VFlowContainer VSeparator
    VSlider VSplitContainer Window])

(def +classes-resource+
  '[AESContext AnimatedTexture Animation AnimationLibrary ArrayMesh
    ArrayOccluder3D AtlasTexture AudioBusLayout AudioEffect AudioEffectAmplify
    AudioEffectBandLimitFilter AudioEffectBandPassFilter AudioEffectCapture
    AudioEffectChorus AudioEffectCompressor AudioEffectDelay
    AudioEffectDistortion AudioEffectEQ AudioEffectEQ10 AudioEffectEQ21
    AudioEffectEQ6 AudioEffectFilter AudioEffectHardLimiter
    AudioEffectHighPassFilter AudioEffectHighShelfFilter AudioEffectInstance
    AudioEffectLimiter AudioEffectLowPassFilter AudioEffectLowShelfFilter
    AudioEffectNotchFilter AudioEffectPanner AudioEffectPhaser
    AudioEffectPitchShift AudioEffectRecord AudioEffectReverb
    AudioEffectSpectrumAnalyzer AudioEffectSpectrumAnalyzerInstance
    AudioEffectStereoEnhance AudioSample AudioSamplePlayback AudioStream
    AudioStreamGenerator AudioStreamGeneratorPlayback AudioStreamInteractive
    AudioStreamMP3 AudioStreamMicrophone AudioStreamOggVorbis AudioStreamPlayback
    AudioStreamPlaybackInteractive AudioStreamPlaybackOggVorbis
    AudioStreamPlaybackPlaylist AudioStreamPlaybackPolyphonic
    AudioStreamPlaybackResampled AudioStreamPlaybackSynchronized
    AudioStreamPlaylist AudioStreamPolyphonic AudioStreamRandomizer
    AudioStreamSynchronized AudioStreamWAV BaseMaterial3D BitMap BoneMap
    BoxMesh CameraAttributes CameraAttributesPhysical CameraAttributesPractical
    CanvasItemMaterial CapsuleMesh CapsuleShape2D CapsuleShape3D CircleShape2D
    CodeHighlighter Compositor CompositorEffect ConcavePolygonShape3D
    ConfigFile ConvexPolygonShape2D ConvexPolygonShape3D CryptoKey Curve
    Curve2D Curve3D CurveTexture CylinderMesh CylinderShape3D
    DecalAtlasProjection DirectionalLight2DImageBasedDecal
    DirectionalLight2DTextureBasedDecal EditorNode3DGizmoPlugin
    EditorResourceConversionPlugin EditorSceneFormatImporter
    EditorScenePostImportPlugin EditorScript EditorTranslationParserPlugin
    Environment ExternalTexture FastNoiseLite Font FileAccess FontFile
    FontVariation FXAA GDExtension GDExtensionExportPlugin
    GDExtensionFormatLoader GDExtensionLibraryResourceLoader GDScript
    GLTFAccessor GLTFAnimation GLTFBufferView GLTFCamera GLTFDocument
    GLTFDocumentExtension GLTFDocumentExtensionConvertImporterMesh
    GLTFLight GLTFMesh GLTFNode GLTFPhysicsBody GLTFPhysicsShape GLTFSkin
    GLTFSpecGloss GLTFTexture GLTFTextureSampler Gradient GradientTexture1D
    GradientTexture2D HeightMapShape3D Image ImageTexture ImageTexture3D
    ImmediateMesh ImporterMesh InputEvent InputEventAction InputEventFromWindow
    InputEventGesture InputEventJoypadButton InputEventJoypadMotion
    InputEventKey InputEventMagnifyGesture InputEventMidi InputEventMouse
    InputEventMouseButton InputEventMouseMotion InputEventPanGesture
    InputEventScreenDrag InputEventScreenTouch InputEventShortcut InputEventWithModifiers
    JSON JSONRPC LabelSettings LightmapGIData Material Mesh MeshLibrary
    MissingResource MultiMesh MultiMeshInstance3D NavigationMesh
    NavigationPolygon NinePatchRect Noise Texture2D Texture2DArray Texture3D
    TextureLayered Theme Tween UndoRedo VideoStream VisualShader
    VisualShaderNode VisualShaderNodeBillboard VisualShaderNodeBooleanConstant
    VisualShaderNodeBooleanParameter VisualShaderNodeClamp
    VisualShaderNodeColorConstant VisualShaderNodeColorFunc
    VisualShaderNodeColorOp VisualShaderNodeColorParameter
    VisualShaderNodeComment VisualShaderNodeCompare VisualShaderNodeConstant
    VisualShaderNodeCurveTexture VisualShaderNodeCurveXYZTexture
    VisualShaderNodeCustom VisualShaderNodeDerivativeFunc
    VisualShaderNodeDeterminant VisualShaderNodeDistanceFade
    VisualShaderNodeDotProduct VisualShaderNodeExpression
    VisualShaderNodeFaceForward VisualShaderNodeFloatConstant
    VisualShaderNodeFloatFunc VisualShaderNodeFloatOp VisualShaderNodeFloatParameter
    VisualShaderNodeFrame VisualShaderNodeFresnel VisualShaderNodeGlobalExpression
    VisualShaderNodeIf VisualShaderNodeInput VisualShaderNodeIntConstant
    VisualShaderNodeIntFunc VisualShaderNodeIntOp VisualShaderNodeIntParameter
    VisualShaderNodeIs VisualShaderNodeLinearSceneDepth
    VisualShaderNodeMix VisualShaderNodeMultiplyAdd
    VisualShaderNodeOuterProduct VisualShaderNodeOutput
    VisualShaderNodeParticleAccelerator VisualShaderNodeParticleBoxEmitter
    VisualShaderNodeParticleConeVelocity VisualShaderNodeParticleEmit
    VisualShaderNodeParticleEmitter VisualShaderNodeParticleMeshEmitter
    VisualShaderNodeParticleMultiplyByAxisAngle
    VisualShaderNodeParticleRandom VisualShaderNodeParticleRingEmitter
    VisualShaderNodeParticleSphereEmitter VisualShaderNodeParticleSystem
    VisualShaderNodeProximityFade VisualShaderNodeResizableBase
    VisualShaderNodeRotationByAxis VisualShaderNodeSDFRaymarch
    VisualShaderNodeSDFToScreenUV VisualShaderNodeScreenUVToSDF
    VisualShaderNodeSmoothStep VisualShaderNodeStep VisualShaderNodeSwitch
    VisualShaderNodeTexture VisualShaderNodeTexture2DArray
    VisualShaderNodeTexture2DArrayParameter VisualShaderNodeTexture2DParameter
    VisualShaderNodeTexture3D VisualShaderNodeTexture3DParameter
    VisualShaderNodeTextureParameter VisualShaderNodeTextureParameterTriplanar
    VisualShaderNodeTransformCompose VisualShaderNodeTransformConstant
    VisualShaderNodeTransformFunc VisualShaderNodeTransformOp
    VisualShaderNodeTransformParameter VisualShaderNodeUVFunc
    VisualShaderNodeUVPolarCoord VisualShaderNodeVaryingGetter
    VisualShaderNodeVaryingSetter VisualShaderNodeVec2Constant
    VisualShaderNodeVec2Parameter VisualShaderNodeVec3Constant
    VisualShaderNodeVec3Parameter VisualShaderNodeVec4Constant
    VisualShaderNodeVec4Parameter VisualShaderNodeVectorBase
    VisualShaderNodeVectorCompose VisualShaderNodeVectorDecompose
    VisualShaderNodeVectorDistance VisualShaderNodeVectorFunc
    VisualShaderNodeVectorLen VisualShaderNodeVectorOp
    VisualShaderNodeWorldPositionFromDepth WorldBoundaryShape2D
    WorldBoundaryShape3D XRVRS])

(def +classes-refcounted+
  '[AStar2D AStar3D AStarGrid2D AESContext AnimatedTexture Animation
    AnimationLibrary AnimationNode AnimationNodeAdd2 AnimationNodeAdd3
    AnimationNodeAnimation AnimationNodeBlend2 AnimationNodeBlend3
    AnimationNodeBlendSpace1D AnimationNodeBlendSpace2D AnimationNodeBlendTree
    AnimationNodeExtension AnimationNodeOneShot AnimationNodeOutput
    AnimationNodeStateMachine AnimationNodeStateMachinePlayback
    AnimationNodeStateMachineTransition AnimationNodeSub2 AnimationNodeSync
    AnimationNodeTimeScale AnimationNodeTimeSeek AnimationNodeTransition
    AnimationRootNode ArrayMesh ArrayOccluder3D AtlasTexture AudioBusLayout
    AudioEffect AudioEffectAmplify AudioEffectBandLimitFilter
    AudioEffectBandPassFilter AudioEffectCapture AudioEffectChorus
    AudioEffectCompressor AudioEffectDelay AudioEffectDistortion AudioEffectEQ
    AudioEffectEQ10 AudioEffectEQ21 AudioEffectEQ6 AudioEffectFilter
    AudioEffectHardLimiter AudioEffectHighPassFilter AudioEffectHighShelfFilter
    AudioEffectInstance AudioEffectLimiter AudioEffectLowPassFilter
    AudioEffectLowShelfFilter AudioEffectNotchFilter AudioEffectPanner
    AudioEffectPhaser AudioEffectPitchShift AudioEffectRecord AudioEffectReverb
    AudioEffectSpectrumAnalyzer AudioEffectSpectrumAnalyzerInstance
    AudioEffectStereoEnhance AudioSample AudioSamplePlayback AudioStream
    AudioStreamGenerator AudioStreamGeneratorPlayback AudioStreamInteractive
    AudioStreamMP3 AudioStreamMicrophone AudioStreamOggVorbis AudioStreamPlayback
    AudioStreamPlaybackInteractive AudioStreamPlaybackOggVorbis
    AudioStreamPlaybackPlaylist AudioStreamPlaybackPolyphonic
    AudioStreamPlaybackResampled AudioStreamPlaybackSynchronized
    AudioStreamPlaylist AudioStreamPolyphonic AudioStreamRandomizer
    AudioStreamSynchronized AudioStreamWAV BaseMaterial3D BitMap BoneMap
    BoxMesh CameraAttributes CameraAttributesPhysical CameraAttributesPractical
    CanvasItemMaterial CapsuleMesh CapsuleShape2D CapsuleShape3D CircleShape2D
    CodeHighlighter Compositor CompositorEffect ConcavePolygonShape3D
    ConfigFile ConvexPolygonShape2D ConvexPolygonShape3D CryptoKey Curve
    Curve2D Curve3D CurveTexture CylinderMesh CylinderShape3D
    DecalAtlasProjection DirectionalLight2DImageBasedDecal
    DirectionalLight2DTextureBasedDecal EditorNode3DGizmoPlugin
    EditorResourceConversionPlugin EditorSceneFormatImporter
    EditorScenePostImportPlugin EditorScript EditorTranslationParserPlugin
    Environment ExternalTexture FastNoiseLite Font FileAccess FontFile
    FontVariation FXAA GDExtension GDExtensionExportPlugin
    GDExtensionFormatLoader GDExtensionLibraryResourceLoader GDScript
    GLTFAccessor GLTFAnimation GLTFBufferView GLTFCamera GLTFDocument
    GLTFDocumentExtension GLTFDocumentExtensionConvertImporterMesh GLTFLight
    GLTFMesh GLTFNode GLTFPhysicsBody GLTFPhysicsShape GLTFSkin GLTFSpecGloss
    GLTFTexture GLTFTextureSampler Gradient GradientTexture1D GradientTexture2D
    HeightMapShape3D Image ImageTexture ImageTexture3D ImmediateMesh
    ImporterMesh InputEvent InputEventAction InputEventFromWindow
    InputEventGesture InputEventJoypadButton InputEventJoypadMotion InputEventKey
    InputEventMagnifyGesture InputEventMidi InputEventMouse InputEventMouseButton
    InputEventMouseMotion InputEventPanGesture InputEventScreenDrag
    InputEventScreenTouch InputEventShortcut InputEventWithModifiers JSON JSONRPC
    LabelSettings LightmapGIData Material Mesh MeshLibrary MissingResource
    MultiMesh MultiMeshInstance3D NavigationMesh NavigationPolygon NinePatchRect
    Noise Texture2D Texture2DArray Texture3D TextureLayered Theme Tween UndoRedo
    VideoStream VisualShader VisualShaderNode VisualShaderNodeBillboard
    VisualShaderNodeBooleanConstant VisualShaderNodeBooleanParameter
    VisualShaderNodeClamp VisualShaderNodeColorConstant VisualShaderNodeColorFunc
    VisualShaderNodeColorOp VisualShaderNodeColorParameter VisualShaderNodeComment
    VisualShaderNodeCompare VisualShaderNodeConstant
    VisualShaderNodeCurveTexture VisualShaderNodeCurveXYZTexture
    VisualShaderNodeCustom VisualShaderNodeDerivativeFunc
    VisualShaderNodeDeterminant VisualShaderNodeDistanceFade
    VisualShaderNodeDotProduct VisualShaderNodeExpression
    VisualShaderNodeFaceForward VisualShaderNodeFloatConstant
    VisualShaderNodeFloatFunc VisualShaderNodeFloatOp
    VisualShaderNodeFloatParameter VisualShaderNodeFrame VisualShaderNodeFresnel
    VisualShaderNodeGlobalExpression VisualShaderNodeIf VisualShaderNodeInput
    VisualShaderNodeIntConstant VisualShaderNodeIntFunc VisualShaderNodeIntOp
    VisualShaderNodeIntParameter VisualShaderNodeIs
    VisualShaderNodeLinearSceneDepth VisualShaderNodeMix
    VisualShaderNodeMultiplyAdd VisualShaderNodeOuterProduct
    VisualShaderNodeOutput VisualShaderNodeParticleAccelerator
    VisualShaderNodeParticleBoxEmitter VisualShaderNodeParticleConeVelocity
    VisualShaderNodeParticleEmit VisualShaderNodeParticleEmitter
    VisualShaderNodeParticleMeshEmitter
    VisualShaderNodeParticleMultiplyByAxisAngle VisualShaderNodeParticleRandom
    VisualShaderNodeParticleRingEmitter VisualShaderNodeParticleSphereEmitter
    VisualShaderNodeParticleSystem VisualShaderNodeProximityFade
    VisualShaderNodeResizableBase VisualShaderNodeRotationByAxis
    VisualShaderNodeSDFRaymarch VisualShaderNodeSDFToScreenUV
    VisualShaderNodeScreenUVToSDF VisualShaderNodeSmoothStep VisualShaderNodeStep
    VisualShaderNodeSwitch VisualShaderNodeTexture VisualShaderNodeTexture2DArray
    VisualShaderNodeTexture2DArrayParameter VisualShaderNodeTexture2DParameter
    VisualShaderNodeTexture3D VisualShaderNodeTexture3DParameter
    VisualShaderNodeTextureParameter VisualShaderNodeTextureParameterTriplanar
    VisualShaderNodeTransformCompose VisualShaderNodeTransformConstant
    VisualShaderNodeTransformFunc VisualShaderNodeTransformOp
    VisualShaderNodeTransformParameter VisualShaderNodeUVFunc
    VisualShaderNodeUVPolarCoord VisualShaderNodeVaryingGetter
    VisualShaderNodeVaryingSetter VisualShaderNodeVec2Constant
    VisualShaderNodeVec2Parameter VisualShaderNodeVec3Constant
    VisualShaderNodeVec3Parameter VisualShaderNodeVec4Constant
    VisualShaderNodeVec4Parameter VisualShaderNodeVectorBase
    VisualShaderNodeVectorCompose VisualShaderNodeVectorDecompose
    VisualShaderNodeVectorDistance VisualShaderNodeVectorFunc
    VisualShaderNodeVectorLen VisualShaderNodeVectorOp
    VisualShaderNodeWorldPositionFromDepth WorldBoundaryShape2D
    WorldBoundaryShape3D XRVRS])

(def +classes-object+
  '[ClassDB Engine GDExtensionManager GodotInstance MainLoop Object OS
    Performance ProjectSettings SceneTree WorkerThreadPool])

(def +classes-mainloop+
  '[MainLoop SceneTree])

(def +classes-renderscenedata+
  '[RenderSceneData RenderSceneDataExtension RenderSceneDataRD])

(def +classes-renderdata+
  '[RenderData RenderDataExtension RenderDataRD])

(def +classes-physicsdirectbodystate2d+
  '[PhysicsDirectBodyState2D PhysicsDirectBodyState2DExtension])

(def +classes-physicsdirectbodystate3d+
  '[PhysicsDirectBodyState3D PhysicsDirectBodyState3DExtension])

(def +classes-physicsdirectspacestate2d+
  '[PhysicsDirectSpaceState2D PhysicsDirectSpaceState2DExtension])

(def +classes-physicsdirectspacestate3d+
  '[PhysicsDirectSpaceState3D PhysicsDirectSpaceState3DExtension])

(def +classes-physicsserver2d+
  '[PhysicsServer2D PhysicsServer2DExtension])

(def +classes-physicsserver3d+
  '[PhysicsServer3D PhysicsServer3DExtension])

(def +classes-openxrextensionwrapper+
  '[OpenXRAndroidThreadSettingsExtension OpenXRExtensionWrapper
    OpenXRExtensionWrapperExtension OpenXRFrameSynthesisExtension
    OpenXRFutureExtension OpenXRRenderModelExtension
    OpenXRSpatialAnchorCapability OpenXRSpatialEntityExtension
    OpenXRSpatialMarkerTrackingCapability OpenXRSpatialPlaneTrackingCapability])

(def +classes-scriptlanguage+
  '[GDScript NativeScript ScriptLanguage])

(def +classes-resourceimporter+
  '[EditorImportPlugin ResourceImporter])

(comment (l/ns:reset))
