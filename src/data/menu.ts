export type MenuItem = {
  id: string
  name: string
  description: string
  price: number
  category: 'Bagels' | 'Plates' | 'Coffee'
  vegetarian: boolean
  badge?: string
  image: string
}

export const menuItems: MenuItem[] = [
  { id: 'sesame', name: 'The Sesame', description: 'Whipped scallion cream cheese, cucumber, dill.', price: 229, category: 'Bagels', vegetarian: true, badge: 'A HOUSE FAVORITE', image: 'https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?auto=format&fit=crop&w=900&q=85' },
  { id: 'sunrise', name: 'Sunrise Club', description: 'Soft egg, cheddar, chili crisp, honey butter.', price: 279, category: 'Bagels', vegetarian: true, badge: 'MORNING PERSON', image: 'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=900&q=85' },
  { id: 'lox', name: 'The Sunday Lox', description: 'Smoked salmon, caper cream cheese, red onion.', price: 399, category: 'Bagels', vegetarian: false, image: 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=85' },
  { id: 'garden', name: 'Little Green Thing', description: 'Avocado, pickled shallot, lemon, seed crunch.', price: 329, category: 'Bagels', vegetarian: true, badge: 'PLANT-BASED', image: 'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=900&q=85' },
  { id: 'tomato', name: 'Tomato & Good Oil', description: 'Heirloom tomato, whipped ricotta, basil oil.', price: 329, category: 'Plates', vegetarian: true, image: 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=900&q=85' },
  { id: 'granola', name: 'A Very Good Bowl', description: 'Brown-butter granola, yogurt, orchard fruit.', price: 259, category: 'Plates', vegetarian: true, image: 'https://images.unsplash.com/photo-1517673132405-a56a62b18caf?auto=format&fit=crop&w=900&q=85' },
  { id: 'latte', name: 'The Slow Latte', description: 'Double espresso, oat milk, a little maple.', price: 169, category: 'Coffee', vegetarian: true, image: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=900&q=85' },
  { id: 'coldbrew', name: 'Window Seat Cold Brew', description: '18-hour steep, chocolate notes, no hurry.', price: 149, category: 'Coffee', vegetarian: true, badge: 'SLOW STEEPED', image: 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?auto=format&fit=crop&w=900&q=85' },
]