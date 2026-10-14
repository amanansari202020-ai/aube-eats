import { Canvas, useFrame } from '@react-three/fiber'
import { ContactShadows, Environment, Float } from '@react-three/drei'
import { useRef } from 'react'
import type { Group } from 'three'

function Bagel() {
  const group = useRef<Group>(null)
  useFrame(({ clock }, delta) => {
    if (group.current) {
      group.current.rotation.y += delta * 0.16
      group.current.rotation.z = Math.sin(clock.elapsedTime * 0.35) * 0.035
    }
  })
  const sesame = Array.from({ length: 55 }, (_, index) => {
    const a = index * 2.39996
    const z = -0.84 + ((index * 37) % 100) / 100 * 1.68
    const angle = a + z * 0.9
    const radius = 1.03 * Math.sqrt(1 - z * z)
    return { position: [Math.cos(angle) * radius, Math.sin(angle) * radius, z] as [number, number, number], rotation: [Math.sin(angle) * 0.75, -Math.cos(angle) * 0.75, angle] as [number, number, number] }
  })
  return <group ref={group} rotation={[0.32, 0.08, -0.18]} scale={0.66}>
    <mesh castShadow receiveShadow><torusGeometry args={[1.12, 0.42, 32, 120]} /><meshStandardMaterial color="#d89447" roughness={0.62} /></mesh>
    <mesh scale={[1, 1, 0.88]}><torusGeometry args={[1.12, 0.405, 28, 120]} /><meshStandardMaterial color="#efbc6c" roughness={0.65} /></mesh>
    {sesame.map((seed, index) => <mesh key={index} position={seed.position} rotation={seed.rotation} scale={[0.055, 0.022, 0.026]} castShadow><capsuleGeometry args={[0.5, 0.8, 3, 8]} /><meshStandardMaterial color={index % 3 === 0 ? '#fff0c4' : '#f6dfa9'} roughness={0.5} /></mesh>)}
  </group>
}

export function BagelScene() {
  const useStatic = window.matchMedia('(prefers-reduced-motion: reduce)').matches || (navigator.hardwareConcurrency > 0 && navigator.hardwareConcurrency <= 4)
  if (useStatic) return <div className="bagel-static" role="img" aria-label="A freshly baked sesame bagel" />
  return <Canvas className="bagel-canvas" shadows dpr={[1, 1.5]} camera={{ position: [0, 0, 4.7], fov: 39 }} fallback={<div className="bagel-static" aria-label="A freshly baked sesame bagel" />}>
    <ambientLight intensity={1.15} /><directionalLight castShadow position={[-3, 5, 4]} intensity={2.4} shadow-mapSize={[1024, 1024]} /><pointLight position={[4, -1, 2]} intensity={0.65} color="#ffd080" />
    <Float speed={1.1} rotationIntensity={0.12} floatIntensity={0.2}><Bagel /></Float>
    <ContactShadows position={[0, -1.25, 0]} opacity={0.22} scale={5} blur={2.8} far={4} /><Environment preset="apartment" />
  </Canvas>
}